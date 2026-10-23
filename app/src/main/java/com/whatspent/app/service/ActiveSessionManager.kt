package com.whatspent.app.service

import android.content.Context
import com.whatspent.app.data.local.entity.ChatSessionEntity
import com.whatspent.app.data.model.ActiveSessionState
import com.whatspent.app.data.repository.WhatSpentRepository
import com.whatspent.app.util.PermissionHelper
import com.whatspent.app.util.TimeFormatUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages active WhatsApp conversation sessions in-memory with zero background polling
 * when idle and highly-throttled Room database persistence.
 *
 * Battery & Performance Architecture:
 * 1. Zero polling when idle: The engine is purely reactive, triggered solely by AccessibilityEvents.
 * 2. Adaptive Ticker Frequency:
 *    - If Floating Pill Overlay is enabled, ticks at 1-second intervals (`delay(1000L)`) to refresh the clock.
 *    - If Floating Pill Overlay is disabled, ticker sleeps in 60-second intervals (`delay(60_000L)`),
 *      drastically reducing CPU wakeups and keeping battery impact near 0%.
 * 3. Throttled Room DB Writes:
 *    - Checkpoints active sessions every 60 seconds (inserting on first checkpoint, updating existing row thereafter).
 *    - Finalizes Room DB write on conversation exit, discarding ephemeral micro-sessions (< 1 second).
 * 4. Deduplicated Daily Limit Alerts:
 *    - Compares cumulative chat time against configured limits and fires a single gentle heads-up per day.
 */
class ActiveSessionManager(
    private val context: Context,
    private val repository: WhatSpentRepository,
    private val overlayManager: FloatingPillOverlayManager,
    private val scope: CoroutineScope,
    private val isStillInActiveConversation: () -> Boolean = { true }
) {

    private val lock = Any()

    @Volatile
    private var activeChatName: String? = null

    @Volatile
    private var activeDisplayName: String? = null

    @Volatile
    private var sessionStartEpochMs: Long = 0L

    @Volatile
    private var lastCheckpointEpochMs: Long = 0L

    @Volatile
    private var currentDbSessionId: Long? = null

    @Volatile
    private var baseChatTodayMsAtCheckpoint: Long = 0L

    @Volatile
    private var baseTodayTotalMsAtCheckpoint: Long = 0L

    @Volatile
    private var dailyLimitMinutesForActiveChat: Int? = null

    private var tickerJob: Job? = null

    /**
     * Checks if there is an ongoing tracked conversation session.
     */
    fun hasActiveSession(): Boolean = synchronized(lock) { activeChatName != null }

    /**
     * Returns the name of the conversation currently being tracked, or null if idle.
     */
    fun getActiveChatName(): String? = synchronized(lock) { activeChatName }

    /**
     * Called when the accessibility tracker detects the user entering a WhatsApp conversation.
     */
    fun onConversationEntered(chatName: String) {
        val trimmedChat = chatName.trim()
        if (trimmedChat.isBlank()) return

        synchronized(lock) {
            if (activeChatName == trimmedChat) {
                // User is already inside this conversation; continue tracking without resetting
                return
            }
        }

        scope.launch(Dispatchers.IO) {
            // If switching from another conversation, finalize and persist the previous session first
            val previousChat = synchronized(lock) { activeChatName }
            if (previousChat != null && previousChat != trimmedChat) {
                finalizeCurrentSessionInternal("Switched conversation to $trimmedChat")
            }

            // Check if this chat has been excluded by the user in settings
            val metadata = repository.getMetadataForChatSync(trimmedChat)
            if (metadata?.isExcluded == true) {
                withContext(Dispatchers.Main) {
                    overlayManager.hide()
                }
                return@launch
            }

            // Query baseline metrics for today to provide instant, accurate initial state
            val todaySummary = repository.getTodaySummaryForService(trimmedChat, unpersistedDeltaMs = 0L)
            val startTime = System.currentTimeMillis()
            val displayName = metadata?.customAlias?.takeIf { it.isNotBlank() } ?: trimmedChat
            val limitMinutes = metadata?.dailyLimitMinutes

            synchronized(lock) {
                activeChatName = trimmedChat
                activeDisplayName = displayName
                sessionStartEpochMs = startTime
                lastCheckpointEpochMs = startTime
                currentDbSessionId = null
                baseTodayTotalMsAtCheckpoint = todaySummary.todayTotalMs
                baseChatTodayMsAtCheckpoint = todaySummary.currentChatTodayTotalMs
                dailyLimitMinutesForActiveChat = limitMinutes
            }

            // Broadcast initial tracking state to repository & foreground notification on session start
            val initialState = ActiveSessionState(
                isTracking = true,
                currentChatName = trimmedChat,
                currentDisplayName = displayName,
                sessionStartTimeMs = startTime,
                currentSessionDurationMs = 0L,
                currentChatTodayTotalMs = baseChatTodayMsAtCheckpoint,
                todayTotalMs = baseTodayTotalMsAtCheckpoint,
                topChatName = todaySummary.topChatName ?: displayName,
                topChatDurationMs = todaySummary.topChatDurationMs,
                unpersistedDeltaMs = 0L
            )
            repository.updateLiveSessionState(initialState)
            NotificationHelper.updateForegroundNotification(context, initialState)

            // Immediately show floating pill if enabled
            if (repository.preferences.isFloatingPillEnabledSync() &&
                PermissionHelper.isOverlayPermissionGranted(context)
            ) {
                val isOverLimit = limitMinutes != null && baseChatTodayMsAtCheckpoint >= (limitMinutes * 60_000L)
                overlayManager.showOrUpdate(
                    chatDisplayName = displayName,
                    sessionDurationMs = 0L,
                    chatTodayTotalMs = baseChatTodayMsAtCheckpoint,
                    isOverLimit = isOverLimit
                )
            }

            // Start adaptive ticker loop
            startSessionTicker(trimmedChat, displayName)
        }
    }

    /**
     * Called when the accessibility tracker detects the user leaving the WhatsApp conversation
     * (e.g. navigated back to chat list, switched apps, or turned off the screen).
     */
    fun onConversationExited(reason: String) {
        scope.launch(Dispatchers.IO) {
            finalizeCurrentSessionInternal(reason)
        }
    }

    /**
     * Internal persistence and teardown logic for active conversation sessions.
     */
    private suspend fun finalizeCurrentSessionInternal(reason: String) {
        val (chatName, startTime, dbSessionId) = synchronized(lock) {
            tickerJob?.cancel()
            tickerJob = null

            val chat = activeChatName ?: return
            val start = sessionStartEpochMs
            val dbId = currentDbSessionId

            activeChatName = null
            activeDisplayName = null
            sessionStartEpochMs = 0L
            lastCheckpointEpochMs = 0L
            currentDbSessionId = null
            baseChatTodayMsAtCheckpoint = 0L
            baseTodayTotalMsAtCheckpoint = 0L
            dailyLimitMinutesForActiveChat = null

            Triple(chat, start, dbId)
        }

        // Hide floating overlay pill immediately
        withContext(Dispatchers.Main) {
            overlayManager.hide()
        }

        val now = System.currentTimeMillis()
        val totalDurationMs = maxOf(0L, now - startTime)

        // Only persist sessions of at least 1 second (filters accidental taps / UI flickers)
        if (totalDurationMs >= MIN_PERSIST_DURATION_MS) {
            val todayDate = TimeFormatUtils.todayDateString(startTime)
            val sessionEntity = ChatSessionEntity(
                id = dbSessionId ?: 0L,
                chatName = chatName,
                startTime = startTime,
                endTime = now,
                durationMs = totalDurationMs,
                date = todayDate
            )

            if (dbSessionId != null && dbSessionId > 0L) {
                repository.updateSession(sessionEntity)
            } else {
                repository.insertSession(sessionEntity)
            }
        }

        // Refresh stopped state and update notification once
        val stoppedSummary = repository.getTodaySummaryForService(null, unpersistedDeltaMs = 0L)
        val stoppedState = stoppedSummary.copy(isTracking = false)
        repository.updateLiveSessionState(stoppedState)
        NotificationHelper.updateForegroundNotification(context, stoppedState)
    }

    /**
     * Spawns the coroutine ticker that controls overlay updates, 60-second DB checkpoints,
     * notification updates, and daily limit evaluations.
     */
    private fun startSessionTicker(chatName: String, displayName: String) {
        tickerJob?.cancel()
        tickerJob = scope.launch(Dispatchers.Default) {
            var lastCheckpointTime = System.currentTimeMillis()
            var lastNotificationTime = System.currentTimeMillis()

            try {
                while (isActive) {
                    val isPillEnabled = repository.preferences.isFloatingPillEnabledSync() &&
                            PermissionHelper.isOverlayPermissionGranted(context)

                    val sleepIntervalMs = if (isPillEnabled) PILL_TICK_INTERVAL_MS else ACTIVE_VERIFY_INTERVAL_MS
                    delay(sleepIntervalMs)

                    // Verify the foreground window is still an active WhatsApp conversation
                    if (!isStillInActiveConversation()) {
                        finalizeCurrentSessionInternal("Foreground window left WhatsApp conversation")
                        return@launch
                    }

                    val now = System.currentTimeMillis()
                    val (sessionDurationMs, unpersistedDeltaMs) = synchronized(lock) {
                        if (activeChatName != chatName) return@launch
                        Pair(now - sessionStartEpochMs, now - lastCheckpointEpochMs)
                    }
                    val totalChatTodayMs = baseChatTodayMsAtCheckpoint + unpersistedDeltaMs
                    val totalTodayMs = baseTodayTotalMsAtCheckpoint + unpersistedDeltaMs

                    // Check daily chat limit
                    checkDailyLimit(chatName, displayName, totalChatTodayMs)

                    // 1. Update Floating Pill UI (or hide if user disabled it in settings)
                    if (isPillEnabled) {
                        val limitMinutes = dailyLimitMinutesForActiveChat
                        val isOverLimit = limitMinutes != null && totalChatTodayMs >= (limitMinutes * 60_000L)
                        overlayManager.showOrUpdate(
                            chatDisplayName = displayName,
                            sessionDurationMs = sessionDurationMs,
                            chatTodayTotalMs = totalChatTodayMs,
                            isOverLimit = isOverLimit
                        )
                    } else {
                        overlayManager.hide()
                    }

                    // 2. Keep in-memory live state fresh without DB queries
                    val currentLive = repository.liveSessionState.value
                    val topChatDuration = maxOf(currentLive.topChatDurationMs, totalChatTodayMs)
                    val topChat = if (totalChatTodayMs >= currentLive.topChatDurationMs) displayName else currentLive.topChatName
                    repository.updateLiveSessionState(
                        ActiveSessionState(
                            isTracking = true,
                            currentChatName = chatName,
                            currentDisplayName = displayName,
                            sessionStartTimeMs = sessionStartEpochMs,
                            currentSessionDurationMs = sessionDurationMs,
                            currentChatTodayTotalMs = totalChatTodayMs,
                            todayTotalMs = totalTodayMs,
                            topChatName = topChat,
                            topChatDurationMs = topChatDuration,
                            unpersistedDeltaMs = unpersistedDeltaMs
                        )
                    )

                    // 3. Periodic 60-second Room DB Checkpoint
                    if (now - lastCheckpointTime >= CHECKPOINT_INTERVAL_MS) {
                        checkpointSession(chatName, now)
                        lastCheckpointTime = now
                        synchronized(lock) {
                            if (activeChatName == chatName) {
                                baseChatTodayMsAtCheckpoint = totalChatTodayMs
                                baseTodayTotalMsAtCheckpoint = totalTodayMs
                            }
                        }
                    }

                    // 4. Periodic Notification Update (throttled to at most once per 60s)
                    if (now - lastNotificationTime >= NOTIFICATION_UPDATE_INTERVAL_MS) {
                        val currentUnpersisted = synchronized(lock) { now - lastCheckpointEpochMs }
                        val summary = repository.getTodaySummaryForService(chatName, currentUnpersisted)
                        NotificationHelper.updateForegroundNotification(context, summary)
                        lastNotificationTime = now
                    }
                }
            } catch (e: CancellationException) {
                // Ticker cancelled normally upon conversation exit
            }
        }
    }

    /**
     * Persists an intermediate checkpoint to the Room database every 60 seconds so no screen
     * time data is lost even if the process is unexpectedly terminated by Android OS.
     */
    private suspend fun checkpointSession(chatName: String, checkpointTimeMs: Long) {
        val (startTime, existingDbId) = synchronized(lock) {
            if (activeChatName != chatName) return
            Pair(sessionStartEpochMs, currentDbSessionId)
        }

        val durationMs = checkpointTimeMs - startTime
        if (durationMs < MIN_PERSIST_DURATION_MS) return

        val todayDate = TimeFormatUtils.todayDateString(startTime)
        val entity = ChatSessionEntity(
            id = existingDbId ?: 0L,
            chatName = chatName,
            startTime = startTime,
            endTime = checkpointTimeMs,
            durationMs = durationMs,
            date = todayDate
        )

        if (existingDbId != null && existingDbId > 0L) {
            repository.updateSession(entity)
        } else {
            val newId = repository.insertSession(entity)
            synchronized(lock) {
                if (activeChatName == chatName) {
                    currentDbSessionId = newId
                }
            }
        }

        synchronized(lock) {
            if (activeChatName == chatName) {
                lastCheckpointEpochMs = checkpointTimeMs
            }
        }
    }

    /**
     * Evaluates whether the cumulative daily duration for this chat has exceeded the daily limit.
     * Fires a gentle heads-up notification deduplicated to at most once per calendar day.
     */
    private fun checkDailyLimit(chatName: String, displayName: String, cumulativeChatTodayMs: Long) {
        val limitMinutes = dailyLimitMinutesForActiveChat ?: return
        val limitThresholdMs = limitMinutes * 60_000L

        if (cumulativeChatTodayMs >= limitThresholdMs) {
            val todayDate = TimeFormatUtils.todayDateString()
            if (!repository.preferences.hasFiredDailyLimitAlertToday(chatName, todayDate)) {
                repository.preferences.markDailyLimitAlertFiredToday(chatName, todayDate)
                val currentMinutes = (cumulativeChatTodayMs / 60_000L).toInt()
                NotificationHelper.sendDailyLimitAlert(context, displayName, limitMinutes, currentMinutes)
            }
        }
    }

    companion object {
        private const val MIN_PERSIST_DURATION_MS = 1_000L
        private const val CHECKPOINT_INTERVAL_MS = 60_000L
        private const val NOTIFICATION_UPDATE_INTERVAL_MS = 60_000L
        private const val PILL_TICK_INTERVAL_MS = 1_000L
        private const val ACTIVE_VERIFY_INTERVAL_MS = 2_000L
    }
}
