package com.whatspent.app.data.model

import androidx.compose.runtime.Immutable
import java.util.Locale

/**
 * Summary of usage metrics for a specific chat today.
 */
@Immutable
data class ChatSummary(
    val chatName: String,
    val displayName: String,
    val durationMs: Long,
    val sessionCount: Int,
    val percentage: Float,
    val colorTag: Int?,
    val dailyLimitMinutes: Int?,
    val isExcluded: Boolean
) {
    val formattedDuration: String
        get() = formatDuration(durationMs)
}

/**
 * Aggregated usage data point for chart display.
 */
@Immutable
data class DailyUsagePoint(
    val date: String,
    val dayLabel: String,
    val totalDurationMs: Long
) {
    val formattedDuration: String
        get() = formatDuration(totalDurationMs)
}

/**
 * Individual session entry displayed in chronological or timeline views.
 */
@Immutable
data class TimelineSessionItem(
    val id: Long,
    val chatName: String,
    val displayName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long,
    val date: String,
    val colorTag: Int?
) {
    val formattedDuration: String
        get() = formatDuration(durationMs)
}

/**
 * State representing the currently running or recently stopped tracking session.
 */
@Immutable
data class ActiveSessionState(
    val isTracking: Boolean = false,
    val currentChatName: String? = null,
    val currentDisplayName: String? = null,
    val sessionStartTimeMs: Long = 0L,
    val currentSessionDurationMs: Long = 0L,
    val currentChatTodayTotalMs: Long = 0L,
    val todayTotalMs: Long = 0L,
    val topChatName: String? = null,
    val topChatDurationMs: Long = 0L,
    val unpersistedDeltaMs: Long = 0L
)

/**
 * Comprehensive UI state for the main Dashboard screen.
 */
@Immutable
data class DashboardUiState(
    val todayTotalMs: Long = 0L,
    val yesterdayTotalMs: Long = 0L,
    val percentChangeVsYesterday: Int? = null,
    val comparisonText: String = "No data yesterday",
    val chatSummaries: List<ChatSummary> = emptyList(),
    val sevenDayUsage: List<DailyUsagePoint> = emptyList(),
    val activeSession: ActiveSessionState = ActiveSessionState(),
    val isLoading: Boolean = true
) {
    val formattedTodayTotal: String
        get() = formatDuration(todayTotalMs)

    val formattedYesterdayTotal: String
        get() = formatDuration(yesterdayTotalMs)
}

/**
 * Range selector for Chat Detail analytics.
 */
enum class DetailRange(val days: Int, val label: String) {
    DAY(1, "Today"),
    WEEK(7, "7 Days"),
    MONTH(30, "30 Days")
}

/**
 * Complete UI state for the Chat Detail & Settings screen.
 */
@Immutable
data class ChatDetailUiState(
    val chatName: String = "",
    val displayName: String = "",
    val customAlias: String? = null,
    val isExcluded: Boolean = false,
    val dailyLimitMinutes: Int? = null,
    val colorTag: Int? = null,
    val selectedRange: DetailRange = DetailRange.WEEK,
    val totalDurationInRangeMs: Long = 0L,
    val todayDurationMs: Long = 0L,
    val sessionCountInRange: Int = 0,
    val averageSessionMs: Long = 0L,
    val longestSessionMs: Long = 0L,
    val dailyPoints: List<DailyUsagePoint> = emptyList(),
    val recentSessions: List<TimelineSessionItem> = emptyList()
) {
    val formattedTotalDuration: String
        get() = formatDuration(totalDurationInRangeMs)

    val formattedTodayDuration: String
        get() = formatDuration(todayDurationMs)

    val formattedAverageSession: String
        get() = formatDuration(averageSessionMs)

    val formattedLongestSession: String
        get() = formatDuration(longestSessionMs)
}

/**
 * Utility function to format millisecond durations into clean, human-readable strings.
 */
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "0m"
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return when {
        hours > 0 -> {
            if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
        }
        minutes > 0 -> "${minutes}m"
        else -> "${seconds}s"
    }
}

/**
 * Format duration with seconds precision for active timers and session badges.
 */
fun formatDurationWithSeconds(durationMs: Long): String {
    if (durationMs <= 0L) return "00:00"
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
