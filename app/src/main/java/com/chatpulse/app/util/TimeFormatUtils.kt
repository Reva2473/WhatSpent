package com.chatpulse.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Utility object for time, duration, and date formatting across ChatPulse.
 */
object TimeFormatUtils {

    private const val ISO_DATE_PATTERN = "yyyy-MM-dd"

    /**
     * Formats duration into a short human-readable string:
     * Examples: "2h 14m", "18m", "45s", "0m"
     */
    fun formatDurationShort(durationMs: Long): String {
        if (durationMs <= 0L) return "0m"
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
            minutes > 0 -> "${minutes}m"
            seconds > 0 -> "${seconds}s"
            else -> "0m"
        }
    }

    /**
     * Formats duration into a compact format:
     * Examples: "1h 05m", "12m 30s", "45s"
     */
    fun formatDurationCompact(durationMs: Long): String {
        if (durationMs <= 0L) return "0s"
        val totalSeconds = durationMs / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm", hours, minutes)
            minutes > 0 -> if (seconds > 0) String.format(Locale.US, "%dm %02ds", minutes, seconds) else "${minutes}m"
            else -> "${seconds}s"
        }
    }

    /**
     * Formats duration into an active timer clock string:
     * Examples: "04:12" or "1:04:12" (for the live floating pill overlay)
     */
    fun formatTimerClock(durationMs: Long): String {
        val totalSeconds = maxOf(0L, durationMs / 1000)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Formats epoch timestamp to 24-hour time of day:
     * Example: "14:20"
     */
    fun formatTimeOfDay(epochMillis: Long): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(epochMillis))
    }

    /**
     * Formats time range between two timestamps:
     * Example: "14:20 - 14:35"
     */
    fun formatTimeRange(startMillis: Long, endMillis: Long): String {
        return "${formatTimeOfDay(startMillis)} - ${formatTimeOfDay(endMillis)}"
    }

    /**
     * Returns today's ISO date string: "yyyy-MM-dd"
     */
    fun todayDateString(nowMillis: Long = System.currentTimeMillis()): String {
        val formatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US)
        return formatter.format(Date(nowMillis))
    }

    /**
     * Returns yesterday's ISO date string: "yyyy-MM-dd"
     */
    fun yesterdayDateString(nowMillis: Long = System.currentTimeMillis()): String {
        return dateStringDaysAgo(1, nowMillis)
    }

    /**
     * Returns an ISO date string N days in the past: "yyyy-MM-dd"
     */
    fun dateStringDaysAgo(daysAgo: Int, nowMillis: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        val formatter = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US)
        return formatter.format(calendar.time)
    }

    /**
     * Formats an ISO date string into a 3-letter day abbreviation: "Mon", "Tue", etc.
     */
    fun formatShortDayLabel(dateString: String): String {
        return try {
            val date = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).parse(dateString)
            if (date != null) {
                SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            } else {
                ""
            }
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Formats an ISO date string into user-friendly text:
     * "Today, Oct 23" / "Yesterday, Oct 22" / "Fri, Oct 20"
     */
    fun formatReadableDate(dateString: String): String {
        val today = todayDateString()
        val yesterday = yesterdayDateString()
        return try {
            val parsedDate = SimpleDateFormat(ISO_DATE_PATTERN, Locale.US).parse(dateString) ?: return dateString
            when (dateString) {
                today -> "Today, " + SimpleDateFormat("MMM d", Locale.getDefault()).format(parsedDate)
                yesterday -> "Yesterday, " + SimpleDateFormat("MMM d", Locale.getDefault()).format(parsedDate)
                else -> SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(parsedDate)
            }
        } catch (_: Exception) {
            dateString
        }
    }

    /**
     * Calculates the percent change between today and yesterday screen times.
     * Returns a signed integer percentage (+15, -20, 0), or null if yesterday had no usage.
     */
    fun calculatePercentChange(todayMs: Long, yesterdayMs: Long): Int? {
        if (yesterdayMs <= 0L) return null
        val diff = todayMs - yesterdayMs
        val pct = (diff.toDouble() / yesterdayMs.toDouble()) * 100.0
        return pct.roundToInt()
    }

    /**
     * Formats the percentage comparison text against yesterday:
     * "+15% vs yesterday", "-8% vs yesterday", "0% vs yesterday", or "No data yesterday"
     */
    fun formatPercentComparison(todayMs: Long, yesterdayMs: Long): String {
        val pct = calculatePercentChange(todayMs, yesterdayMs) ?: return "No data yesterday"
        return when {
            pct > 0 -> "+$pct% vs yesterday"
            pct < 0 -> "$pct% vs yesterday"
            else -> "0% vs yesterday"
        }
    }
}
