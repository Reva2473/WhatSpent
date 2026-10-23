package com.chatpulse.app.data.repository

import android.content.Context
import com.chatpulse.app.data.local.ChatPulseDatabase
import com.chatpulse.app.data.local.dao.ChatMetadataDao
import com.chatpulse.app.data.local.dao.ChatSessionDao
import com.chatpulse.app.data.local.entity.ChatMetadataEntity
import com.chatpulse.app.data.local.entity.ChatSessionEntity
import com.chatpulse.app.data.model.ActiveSessionState
import com.chatpulse.app.data.model.ChatDetailUiState
import com.chatpulse.app.data.model.ChatSummary
import com.chatpulse.app.data.model.DailyUsagePoint
import com.chatpulse.app.data.model.DashboardUiState
import com.chatpulse.app.data.model.DetailRange
import com.chatpulse.app.data.model.TimelineSessionItem
import com.chatpulse.app.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Single source of truth repository managing chat usage sessions, metadata,
 * real-time active session states, and dashboard/detail analytics.
 */
class ChatPulseRepository(
    private val sessionDao: ChatSessionDao,
    private val metadataDao: ChatMetadataDao,
    val preferences: AppPreferencesRepository
) {

    private val _liveSessionState = MutableStateFlow(ActiveSessionState())
    val liveSessionState: StateFlow<ActiveSessionState> = _liveSessionState.asStateFlow()

    /**
     * Broadcasts the real-time active tracking session state received from the foreground tracker.
     */
    fun updateLiveSessionState(state: ActiveSessionState) {
        _liveSessionState.value = state
    }

    /**
     * Provides a reactive stream of Dashboard UI state, combining today's aggregations,
     * yesterday's totals, 7-day usage trends, chat metadata, and live unpersisted tracking deltas.
     */
    fun observeDashboardState(): Flow<DashboardUiState> {
        val todayDate = getTodayDateString()
        val yesterdayDate = getDateStringDaysAgo(1)
        val sevenDaysAgoDate = getDateStringDaysAgo(6)

        return combine(
            sessionDao.observeChatAggregationsForDate(todayDate),
            sessionDao.observeTotalDurationForDate(yesterdayDate),
            sessionDao.observeDailyTotalsInRange(sevenDaysAgoDate, todayDate),
            metadataDao.observeAllMetadata(),
            _liveSessionState
        ) { todayAggs, yesterdayTotal, sevenDaysTotals, metadataList, liveState ->
            val metaMap = metadataList.associateBy { it.chatName }
            val excludedChats = metadataList.filter { it.isExcluded }.map { it.chatName }.toSet()

            // Map chatName -> Pair(totalDurationMs, sessionCount)
            val chatMap = mutableMapOf<String, Pair<Long, Int>>()
            for (agg in todayAggs) {
                chatMap[agg.chatName] = Pair(agg.totalDurationMs, agg.sessionCount)
            }

            // Factor in live unpersisted session time without double counting
            if (liveState.isTracking && !liveState.currentChatName.isNullOrBlank()) {
                val activeChat = liveState.currentChatName
                val delta = liveState.unpersistedDeltaMs.coerceAtLeast(0L)
                if (delta > 0L) {
                    val current = chatMap[activeChat]
                    if (current != null) {
                        chatMap[activeChat] = Pair(current.first + delta, current.second)
                    } else {
                        chatMap[activeChat] = Pair(delta, 1)
                    }
                }
            }

            // Excluded chats do not count towards overall dashboard total usage
            val nonExcludedEntries = chatMap.filterKeys { it !in excludedChats }
            val todayTotalMs = nonExcludedEntries.values.sumOf { it.first }

            // Build chat summaries with percentage of total usage
            val summaries = chatMap.map { (chatName, pair) ->
                val (duration, count) = pair
                val meta = metaMap[chatName]
                val isExcluded = meta?.isExcluded ?: (chatName in excludedChats)
                val percentage = if (!isExcluded && todayTotalMs > 0L) {
                    (duration.toFloat() / todayTotalMs.toFloat()) * 100f
                } else {
                    0f
                }

                ChatSummary(
                    chatName = chatName,
                    displayName = meta?.customAlias?.takeIf { it.isNotBlank() } ?: chatName,
                    durationMs = duration,
                    sessionCount = count,
                    percentage = percentage,
                    colorTag = meta?.colorTag,
                    dailyLimitMinutes = meta?.dailyLimitMinutes,
                    isExcluded = isExcluded
                )
            }.sortedWith(compareBy({ it.isExcluded }, { -it.durationMs }))

            // Calculate percentage comparison vs yesterday
            val (percentChangeVsYesterday, comparisonText) = if (yesterdayTotal > 0L) {
                val diff = todayTotalMs - yesterdayTotal
                val percent = ((diff.toDouble() / yesterdayTotal.toDouble()) * 100).toInt()
                val text = when {
                    percent > 0 -> "+$percent% vs yesterday"
                    percent < 0 -> "$percent% vs yesterday"
                    else -> "Same as yesterday"
                }
                Pair(percent, text)
            } else {
                Pair(null, "No data yesterday")
            }

            // Build 7-day usage points (oldest to newest)
            val dailyTotalsMap = sevenDaysTotals.associate { it.date to it.totalDurationMs }
            val sevenDayUsage = (6 downTo 0).map { daysAgo ->
                val dateStr = getDateStringDaysAgo(daysAgo)
                val dbDuration = dailyTotalsMap[dateStr] ?: 0L
                val finalDuration = if (dateStr == todayDate &&
                    liveState.isTracking &&
                    !liveState.currentChatName.isNullOrBlank() &&
                    liveState.currentChatName !in excludedChats
                ) {
                    dbDuration + liveState.unpersistedDeltaMs.coerceAtLeast(0L)
                } else {
                    dbDuration
                }
                val label = if (dateStr == todayDate) "Today" else getDayOfWeekAbbreviation(dateStr)
                DailyUsagePoint(
                    date = dateStr,
                    dayLabel = label,
                    totalDurationMs = finalDuration
                )
            }

            DashboardUiState(
                todayTotalMs = todayTotalMs,
                yesterdayTotalMs = yesterdayTotal,
                percentChangeVsYesterday = percentChangeVsYesterday,
                comparisonText = comparisonText,
                chatSummaries = summaries,
                sevenDayUsage = sevenDayUsage,
                activeSession = liveState,
                isLoading = false
            )
        }
    }

    /**
     * Observes all sessions for a specific date with metadata decoration (custom aliases & colors).
     */
    fun observeTimelineForDate(date: String): Flow<List<TimelineSessionItem>> {
        return sessionDao.observeSessionsByDate(date)
            .combine(metadataDao.observeAllMetadata()) { sessions, metadataList ->
                val metaMap = metadataList.associateBy { it.chatName }
                sessions.map { session ->
                    val meta = metaMap[session.chatName]
                    TimelineSessionItem(
                        id = session.id,
                        chatName = session.chatName,
                        displayName = meta?.customAlias?.takeIf { it.isNotBlank() } ?: session.chatName,
                        startTime = session.startTime,
                        endTime = session.endTime,
                        durationMs = session.durationMs,
                        date = session.date,
                        colorTag = meta?.colorTag
                    )
                }
            }
    }

    /**
     * Observes distinct recorded dates for timeline browsing, guaranteeing today is included.
     */
    fun observeAvailableDates(): Flow<List<String>> {
        val today = getTodayDateString()
        return sessionDao.observeAvailableDates().map { dates ->
            if (!dates.contains(today)) {
                listOf(today) + dates
            } else {
                dates
            }
        }
    }

    /**
     * Observes deep usage analytics, chart points, and recent sessions for a single chat.
     */
    fun observeChatDetail(chatName: String, range: DetailRange): Flow<ChatDetailUiState> {
        val todayDate = getTodayDateString()
        val startDate = getDateStringDaysAgo(range.days - 1)
        val endDate = todayDate

        return combine(
            metadataDao.observeMetadataForChat(chatName),
            sessionDao.observeSessionsForChatSince(chatName, startDate),
            sessionDao.observeChatDailyTotalsInRange(chatName, startDate, endDate),
            sessionDao.observeChatDurationForDate(chatName, todayDate),
            _liveSessionState
        ) { metadata, sessions, dailyTotals, todayDbDuration, liveState ->
            val isCurrentActive = liveState.isTracking && liveState.currentChatName == chatName
            val activeDelta = if (isCurrentActive) liveState.unpersistedDeltaMs.coerceAtLeast(0L) else 0L

            val todayDurationMs = todayDbDuration + activeDelta
            val totalDurationInRangeMs = dailyTotals.sumOf { it.totalDurationMs } + activeDelta

            val activeSessionCountIncrement = if (isCurrentActive && activeDelta > 0L && sessions.none { it.id == 0L }) 1 else 0
            val sessionCountInRange = sessions.size + activeSessionCountIncrement

            val averageSessionMs = if (sessionCountInRange > 0) {
                totalDurationInRangeMs / sessionCountInRange
            } else {
                0L
            }

            val maxDbSession = sessions.maxOfOrNull { it.durationMs } ?: 0L
            val longestSessionMs = maxOf(
                maxDbSession,
                if (isCurrentActive) liveState.currentSessionDurationMs else 0L
            )

            val dailyPointsMap = dailyTotals.associate { it.date to it.totalDurationMs }
            val dailyPoints = ((range.days - 1) downTo 0).map { daysAgo ->
                val dateStr = getDateStringDaysAgo(daysAgo)
                val base = dailyPointsMap[dateStr] ?: 0L
                val duration = if (dateStr == todayDate) base + activeDelta else base
                val label = if (dateStr == todayDate) "Today" else getDayOfWeekAbbreviation(dateStr)
                DailyUsagePoint(
                    date = dateStr,
                    dayLabel = label,
                    totalDurationMs = duration
                )
            }

            val displayName = metadata?.customAlias?.takeIf { it.isNotBlank() } ?: chatName
            val recentSessions = sessions.map { session ->
                TimelineSessionItem(
                    id = session.id,
                    chatName = session.chatName,
                    displayName = displayName,
                    startTime = session.startTime,
                    endTime = session.endTime,
                    durationMs = session.durationMs,
                    date = session.date,
                    colorTag = metadata?.colorTag
                )
            }

            ChatDetailUiState(
                chatName = chatName,
                displayName = displayName,
                customAlias = metadata?.customAlias,
                isExcluded = metadata?.isExcluded ?: false,
                dailyLimitMinutes = metadata?.dailyLimitMinutes,
                colorTag = metadata?.colorTag,
                selectedRange = range,
                totalDurationInRangeMs = totalDurationInRangeMs,
                todayDurationMs = todayDurationMs,
                sessionCountInRange = sessionCountInRange,
                averageSessionMs = averageSessionMs,
                longestSessionMs = longestSessionMs,
                dailyPoints = dailyPoints,
                recentSessions = recentSessions
            )
        }
    }

    /**
     * Updates or creates metadata configuration for a specific chat.
     */
    suspend fun updateChatMetadata(
        chatName: String,
        isExcluded: Boolean? = null,
        dailyLimitMinutes: Int? = null,
        clearDailyLimit: Boolean = false,
        customAlias: String? = null,
        colorTag: Int? = null
    ) {
        val existing = metadataDao.getMetadataForChatSync(chatName)
        val updated = if (existing != null) {
            existing.copy(
                isExcluded = isExcluded ?: existing.isExcluded,
                dailyLimitMinutes = if (clearDailyLimit) null else (dailyLimitMinutes ?: existing.dailyLimitMinutes),
                customAlias = customAlias ?: existing.customAlias,
                colorTag = colorTag ?: existing.colorTag
            )
        } else {
            ChatMetadataEntity(
                chatName = chatName,
                isExcluded = isExcluded ?: false,
                dailyLimitMinutes = if (clearDailyLimit) null else dailyLimitMinutes,
                customAlias = customAlias,
                colorTag = colorTag
            )
        }
        metadataDao.upsertMetadata(updated)
    }

    suspend fun getMetadataForChatSync(chatName: String): ChatMetadataEntity? {
        return metadataDao.getMetadataForChatSync(chatName)
    }

    suspend fun getExcludedChatsSync(): Set<String> {
        return metadataDao.getExcludedChatsSync().toSet()
    }

    suspend fun insertSession(session: ChatSessionEntity): Long {
        return sessionDao.insertSession(session)
    }

    suspend fun updateSession(session: ChatSessionEntity) {
        sessionDao.updateSession(session)
    }

    /**
     * Calculates real-time usage summary for foreground notification & accessibility service,
     * filtering excluded chats and incorporating active session deltas.
     */
    suspend fun getTodaySummaryForService(
        activeChatName: String?,
        unpersistedDeltaMs: Long = 0L
    ): ActiveSessionState {
        val todayDate = getTodayDateString()
        val aggregations = sessionDao.getChatAggregationsForDateSync(todayDate)
        val excludedChats = metadataDao.getExcludedChatsSync().toSet()

        val chatDurationMap = aggregations
            .filter { it.chatName !in excludedChats }
            .associate { it.chatName to it.totalDurationMs }
            .toMutableMap()

        val safeDelta = unpersistedDeltaMs.coerceAtLeast(0L)
        if (activeChatName != null && activeChatName !in excludedChats) {
            chatDurationMap[activeChatName] = (chatDurationMap[activeChatName] ?: 0L) + safeDelta
        }

        val todayTotalMs = chatDurationMap.values.sum()
        val topEntry = chatDurationMap.maxByOrNull { it.value }
        val topChatName = topEntry?.key
        val topChatDurationMs = topEntry?.value ?: 0L

        val currentChatTodayTotalMs = if (activeChatName != null) {
            chatDurationMap[activeChatName] ?: safeDelta
        } else {
            0L
        }

        val meta = if (activeChatName != null) metadataDao.getMetadataForChatSync(activeChatName) else null
        val displayName = meta?.customAlias?.takeIf { it.isNotBlank() } ?: activeChatName

        return ActiveSessionState(
            isTracking = activeChatName != null,
            currentChatName = activeChatName,
            currentDisplayName = displayName,
            currentSessionDurationMs = safeDelta,
            currentChatTodayTotalMs = currentChatTodayTotalMs,
            todayTotalMs = todayTotalMs,
            topChatName = topChatName,
            topChatDurationMs = topChatDurationMs,
            unpersistedDeltaMs = safeDelta
        )
    }

    /**
     * Populates demo data if database is currently empty.
     */
    suspend fun seedSampleDataIfNeeded() {
        val today = getTodayDateString()
        val totalToday = sessionDao.getTotalDurationForDateSync(today)
        if (totalToday == 0L) {
            seedDemoData()
        }
    }

    /**
     * Seeds realistic chat sessions and metadata for the last 7 days.
     */
    suspend fun seedDemoData() {
        // Chat definitions
        val designTeam = "Product Design Team"
        val alex = "Alex Rivera"
        val family = "Mom & Family"
        val backend = "Backend Guild"
        val sarah = "Sarah Chen"

        // Seed Metadata
        metadataDao.upsertMetadata(
            ChatMetadataEntity(
                chatName = designTeam,
                isExcluded = false,
                dailyLimitMinutes = 45,
                customAlias = "Design Team",
                colorTag = 0xFF3F51B5.toInt() // Indigo
            )
        )
        metadataDao.upsertMetadata(
            ChatMetadataEntity(
                chatName = alex,
                isExcluded = false,
                dailyLimitMinutes = 30,
                customAlias = "Alex (Project Lead)",
                colorTag = 0xFFE91E63.toInt() // Pink
            )
        )
        metadataDao.upsertMetadata(
            ChatMetadataEntity(
                chatName = family,
                isExcluded = false,
                dailyLimitMinutes = null,
                customAlias = "Family Group",
                colorTag = 0xFF4CAF50.toInt() // Green
            )
        )
        metadataDao.upsertMetadata(
            ChatMetadataEntity(
                chatName = backend,
                isExcluded = false,
                dailyLimitMinutes = 60,
                customAlias = "Backend Guild",
                colorTag = 0xFFFF9800.toInt() // Orange
            )
        )
        metadataDao.upsertMetadata(
            ChatMetadataEntity(
                chatName = sarah,
                isExcluded = false,
                dailyLimitMinutes = 25,
                customAlias = null,
                colorTag = 0xFF9C27B0.toInt() // Purple
            )
        )

        // Seed 7 days of realistic sessions
        val calendar = Calendar.getInstance()
        val baseTimeMs = System.currentTimeMillis()

        for (daysAgo in 6 downTo 0) {
            val dateStr = getDateStringDaysAgo(daysAgo)
            calendar.timeInMillis = baseTimeMs
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)

            // Morning sessions
            createSampleSession(calendar, 9, 15, 22, designTeam, dateStr)
            createSampleSession(calendar, 10, 5, 12, alex, dateStr)
            createSampleSession(calendar, 11, 30, 8, family, dateStr)

            // Afternoon sessions
            createSampleSession(calendar, 14, 0, 32, backend, dateStr)
            createSampleSession(calendar, 15, 15, 15, sarah, dateStr)
            createSampleSession(calendar, 16, 20, 18, designTeam, dateStr)

            // Evening sessions (only on some days for variety)
            if (daysAgo % 2 == 0) {
                createSampleSession(calendar, 19, 10, 20, family, dateStr)
                createSampleSession(calendar, 20, 45, 14, alex, dateStr)
            }
        }
    }

    private suspend fun createSampleSession(
        calendar: Calendar,
        hour: Int,
        minute: Int,
        durationMinutes: Int,
        chatName: String,
        dateStr: String
    ) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startTime = calendar.timeInMillis
        val durationMs = durationMinutes * 60 * 1000L
        val endTime = startTime + durationMs

        sessionDao.insertSession(
            ChatSessionEntity(
                chatName = chatName,
                startTime = startTime,
                endTime = endTime,
                durationMs = durationMs,
                date = dateStr
            )
        )
    }

    /**
     * Clears all session records, metadata settings, and daily alert cache.
     */
    suspend fun clearAllData() {
        sessionDao.deleteAllSessions()
        metadataDao.deleteAllMetadata()
        preferences.clearAllDailyAlertRecords()
        _liveSessionState.value = ActiveSessionState()
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatPulseRepository? = null

        fun getInstance(context: Context): ChatPulseRepository {
            return INSTANCE ?: synchronized(this) {
                val db = ChatPulseDatabase.getInstance(context)
                val prefs = AppPreferencesRepository.getInstance(context)
                INSTANCE ?: ChatPulseRepository(
                    sessionDao = db.chatSessionDao(),
                    metadataDao = db.chatMetadataDao(),
                    preferences = prefs
                ).also { INSTANCE = it }
            }
        }

        fun getTodayDateString(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun getDateStringDaysAgo(daysAgo: Int): String {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(cal.time)
        }

        fun getDayOfWeekAbbreviation(dateString: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = sdf.parse(dateString) ?: return dateString
                val dayFormat = SimpleDateFormat("EEE", Locale.US)
                dayFormat.format(date)
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
