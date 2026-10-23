package com.chatpulse.app.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.Rect
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.chatpulse.app.ChatPulseApplication
import com.chatpulse.app.data.local.ChatPulseDatabase
import com.chatpulse.app.data.model.ActiveSessionState
import com.chatpulse.app.data.preferences.AppPreferencesRepository
import com.chatpulse.app.data.repository.ChatPulseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Low-battery Accessibility Service for WhatsApp & WhatsApp Business conversation tracking.
 *
 * Architecture & Battery Optimizations:
 * 1. Screen Interactive Fast-Path:
 *    Instantly skips event evaluation when the screen is turned off or non-interactive.
 * 2. Event Throttling:
 *    Limits high-frequency `TYPE_WINDOW_CONTENT_CHANGED` inspection to at most once per 350ms
 *    while already tracking an active conversation, preventing CPU spikes during fast scrolling or typing.
 * 3. Two-Tier Resilient Detection:
 *    - Strategy 1: Fast direct View ID lookup on known WhatsApp toolbar title TextViews.
 *    - Strategy 2: Heuristic toolbar & message input bar detector ensuring forward compatibility
 *      across future WhatsApp app updates.
 * 4. Zero Polling:
 *    Idle state incurs 0% CPU overhead, waking only when WhatsApp window events arrive.
 */
class WhatsAppTrackerService : AccessibilityService() {

    private lateinit var repository: ChatPulseRepository
    private lateinit var overlayManager: FloatingPillOverlayManager
    private lateinit var sessionManager: ActiveSessionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var isScreenInteractive = true
    private var lastContentChangeInspectionTimeMs = 0L
    private var lastKnownChatTitle: String? = null

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenInteractive = false
                    sessionManager.onConversationExited("Screen turned off")
                    lastKnownChatTitle = null
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    isScreenInteractive = checkScreenInteractive()
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isInstanceRunning.value = true

        isScreenInteractive = checkScreenInteractive()

        // Register receiver for screen off/on lifecycle
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this,
            screenReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Initialize repository from Application container or fallback singletons
        val app = applicationContext as? ChatPulseApplication
        repository = app?.repository ?: run {
            val db = ChatPulseDatabase.getInstance(this)
            val prefs = AppPreferencesRepository.getInstance(this)
            ChatPulseRepository(db.chatSessionDao(), db.chatMetadataDao(), prefs)
        }

        overlayManager = FloatingPillOverlayManager(this)
        sessionManager = ActiveSessionManager(this, repository, overlayManager, serviceScope)

        // Create notification channels and promote to foreground service
        NotificationHelper.createNotificationChannels(this)
        promoteToForegroundService()

        // Refresh initial foreground notification with today's metrics
        serviceScope.launch {
            val summary = repository.getTodaySummaryForService(activeChatName = null)
            NotificationHelper.updateForegroundNotification(this@WhatsAppTrackerService, summary)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Fast-path: Never process events if screen is off or device is locked
        if (!isScreenInteractive) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        val isTargetApp = packageName == WHATSAPP_PACKAGE || packageName == WHATSAPP_BUSINESS_PACKAGE

        // 1. User switched outside WhatsApp
        if (!isTargetApp) {
            if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                // Ignore transient system UI and soft keyboards
                val isSystemUiOrKeyboard = packageName.startsWith("com.android.systemui") ||
                        packageName.contains("inputmethod") ||
                        packageName.contains("keyboard") ||
                        packageName.contains("gboard")

                if (!isSystemUiOrKeyboard) {
                    sessionManager.onConversationExited("App switched: $packageName")
                    lastKnownChatTitle = null
                }
            }
            return
        }

        // 2. User is inside WhatsApp
        // Throttle content changes during rapid typing or scrolling to save battery
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            val now = SystemClock.uptimeMillis()
            if (sessionManager.hasActiveSession() && now - lastContentChangeInspectionTimeMs < CONTENT_CHANGE_THROTTLE_MS) {
                return
            }
            lastContentChangeInspectionTimeMs = now
        }

        // Check if window changed to a known non-conversation screen
        val windowClassName = event.className?.toString()
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !windowClassName.isNullOrBlank()) {
            val isNonConversation = KNOWN_NON_CONVERSATION_CLASSES.any {
                windowClassName.endsWith(it) || windowClassName.contains(it)
            }
            if (isNonConversation) {
                sessionManager.onConversationExited("Navigated to $windowClassName")
                lastKnownChatTitle = null
                return
            }
        }

        // Inspect window hierarchy
        val rootNode = rootInActiveWindow ?: return
        try {
            val detectedChatTitle = extractActiveConversationTitle(rootNode, windowClassName)
            if (!detectedChatTitle.isNullOrBlank()) {
                lastKnownChatTitle = detectedChatTitle
                sessionManager.onConversationEntered(detectedChatTitle)
            } else {
                // Check if user transitioned to the chat list tabs
                if (isChatListOrTabsScreen(rootNode)) {
                    sessionManager.onConversationExited("Returned to chat list")
                    lastKnownChatTitle = null
                }
            }
        } finally {
            // Note: Modern Android manages AccessibilityNodeInfo recycling internally
        }
    }

    /**
     * Multi-strategy heuristic detector to accurately extract the active conversation title.
     */
    private fun extractActiveConversationTitle(
        rootNode: AccessibilityNodeInfo,
        windowClassName: String?
    ): String? {
        // Strategy 1: Direct WhatsApp View ID Lookup (Fastest, zero traversal)
        val idCandidates = listOf(
            "$WHATSAPP_PACKAGE:id/conversation_contact_name",
            "$WHATSAPP_BUSINESS_PACKAGE:id/conversation_contact_name",
            "$WHATSAPP_PACKAGE:id/conversation_title",
            "$WHATSAPP_BUSINESS_PACKAGE:id/conversation_title"
        )

        for (viewId in idCandidates) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
            if (!nodes.isNullOrEmpty()) {
                for (node in nodes) {
                    val title = node.text?.toString()?.trim()
                    if (isValidChatTitle(title)) {
                        return title
                    }
                }
            }
        }

        // Strategy 2: Toolbar + Conversation Input Verification Heuristic
        // (Resilient against view ID changes and WhatsApp updates)
        if (isConversationScreen(rootNode, windowClassName)) {
            val screenHeight = resources.displayMetrics.heightPixels
            val maxToolbarBottom = (screenHeight * 0.22f).toInt()

            return findToolbarTitleTextView(rootNode, maxToolbarBottom)
        }

        return null
    }

    /**
     * Verifies that the current screen is genuinely an active conversation screen rather than
     * the chat list, status tab, or settings.
     */
    private fun isConversationScreen(
        rootNode: AccessibilityNodeInfo,
        windowClassName: String?
    ): Boolean {
        if (windowClassName?.contains("Conversation", ignoreCase = true) == true) {
            return true
        }

        // Check for presence of chat message compose / input bar
        val inputBarIds = listOf(
            "$WHATSAPP_PACKAGE:id/entry",
            "$WHATSAPP_BUSINESS_PACKAGE:id/entry",
            "$WHATSAPP_PACKAGE:id/input_layout",
            "$WHATSAPP_BUSINESS_PACKAGE:id/input_layout",
            "$WHATSAPP_PACKAGE:id/voice_note_btn",
            "$WHATSAPP_BUSINESS_PACKAGE:id/voice_note_btn",
            "$WHATSAPP_PACKAGE:id/send",
            "$WHATSAPP_BUSINESS_PACKAGE:id/send"
        )

        for (id in inputBarIds) {
            val nodes = rootNode.findAccessibilityNodeInfosByViewId(id)
            if (!nodes.isNullOrEmpty()) {
                return true
            }
        }

        // Check for any EditText located in the lower portion of the screen
        val screenHeight = resources.displayMetrics.heightPixels
        val minInputTop = (screenHeight * 0.60f).toInt()
        val rect = Rect()

        fun hasBottomInput(node: AccessibilityNodeInfo): Boolean {
            if (node.className?.toString()?.contains("EditText", ignoreCase = true) == true) {
                node.getBoundsInScreen(rect)
                if (rect.top >= minInputTop) return true
            }
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                if (hasBottomInput(child)) return true
            }
            return false
        }

        return hasBottomInput(rootNode)
    }

    /**
     * Traverses nodes located in the top toolbar area to find the primary contact / group title.
     */
    private fun findToolbarTitleTextView(
        rootNode: AccessibilityNodeInfo,
        maxToolbarBottom: Int
    ): String? {
        val rect = Rect()

        fun searchNode(node: AccessibilityNodeInfo): String? {
            node.getBoundsInScreen(rect)
            if (rect.bottom > 0 && rect.bottom <= maxToolbarBottom) {
                val className = node.className?.toString() ?: ""
                if (className.contains("TextView", ignoreCase = true) && !className.contains("Button", ignoreCase = true)) {
                    val text = node.text?.toString()?.trim()
                    if (isValidChatTitle(text)) {
                        return text
                    }
                }
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val found = searchNode(child)
                if (found != null) return found
            }
            return null
        }

        return searchNode(rootNode)
    }

    /**
     * Determines whether the current window has transitioned back to the main WhatsApp chat list / tabs.
     */
    private fun isChatListOrTabsScreen(rootNode: AccessibilityNodeInfo): Boolean {
        val fabNodes = rootNode.findAccessibilityNodeInfosByViewId("$WHATSAPP_PACKAGE:id/fab")
        if (!fabNodes.isNullOrEmpty()) {
            return true
        }

        val tabTexts = setOf("chats", "updates", "communities", "calls", "status")
        for (tab in tabTexts) {
            val nodes = rootNode.findAccessibilityNodeInfosByText(tab)
            if (!nodes.isNullOrEmpty()) {
                return true
            }
        }

        return false
    }

    /**
     * Filters out status indicators, subtitle states, and common toolbar action labels.
     */
    private fun isValidChatTitle(title: String?): Boolean {
        if (title.isNullOrBlank()) return false
        if (title.length > 100) return false

        val lower = title.lowercase(Locale.ROOT)

        // Filter out non-title subtitles and status messages
        val isInvalidStatus = lower == "online" ||
                lower.startsWith("typing") ||
                lower.startsWith("recording audio") ||
                lower.startsWith("last seen") ||
                lower.contains("tap here for contact info") ||
                lower.contains("tap here for group info") ||
                lower.contains("click here for contact info") ||
                lower.contains("disappearing messages") ||
                lower == "search" ||
                lower == "search…" ||
                lower == "chats" ||
                lower == "updates" ||
                lower == "communities" ||
                lower == "calls" ||
                lower == "status" ||
                lower == "audio call" ||
                lower == "video call" ||
                lower == "call" ||
                lower == "more options" ||
                lower == "attach" ||
                lower == "message" ||
                lower == "yesterday" ||
                lower == "today"

        return !isInvalidStatus
    }

    private fun checkScreenInteractive(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isInteractive ?: true
    }

    private fun promoteToForegroundService() {
        val initialNotification = NotificationHelper.buildForegroundNotification(
            this,
            ActiveSessionState(isTracking = false)
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NotificationHelper.FOREGROUND_NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                )
            } else {
                startForeground(
                    NotificationHelper.FOREGROUND_NOTIFICATION_ID,
                    initialNotification
                )
            }
        } catch (e: Exception) {
            // Fallback for devices with strict FGS constraints
            try {
                startForeground(
                    NotificationHelper.FOREGROUND_NOTIFICATION_ID,
                    initialNotification
                )
            } catch (inner: Exception) {
                // Handled gracefully
            }
        }
    }

    override fun onInterrupt() {
        sessionManager.onConversationExited("Service interrupted")
        lastKnownChatTitle = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _isInstanceRunning.value = false

        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Handled safely
        }

        sessionManager.onConversationExited("Service destroyed")
        overlayManager.destroy()
        serviceScope.cancel()
    }

    companion object {
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"

        private const val CONTENT_CHANGE_THROTTLE_MS = 350L

        private val _isInstanceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isInstanceRunning.asStateFlow()

        private val KNOWN_NON_CONVERSATION_CLASSES = listOf(
            "HomeActivity",
            "Main",
            "Settings",
            "SettingsActivity",
            "ProfileInfoActivity",
            "ContactPicker",
            "GalleryPickerActivity",
            "StatusPlaybackActivity",
            "VoipActivity",
            "StarredMessagesActivity",
            "ArchivedChatsActivity",
            "MediaGalleryActivity",
            "MediaViewActivity",
            "ViewOnceViewerActivity"
        )
    }
}
