package com.whatspent.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.whatspent.app.MainActivity
import com.whatspent.app.R
import com.whatspent.app.data.model.ActiveSessionState
import com.whatspent.app.util.PermissionHelper
import com.whatspent.app.util.TimeFormatUtils

/**
 * Helper object managing system notification channels, foreground service tracking
 * notifications, and daily chat limit heads-up alerts for WhatSpent.
 */
object NotificationHelper {

    const val TRACKING_CHANNEL_ID = "whatspent_tracking_channel"
    const val ALERTS_CHANNEL_ID = "whatspent_alerts_channel"
    const val FOREGROUND_NOTIFICATION_ID = 1001

    /**
     * Creates required notification channels:
     * 1. Active Tracking (IMPORTANCE_LOW, silent, badge disabled)
     * 2. Daily Chat Limit Alerts (IMPORTANCE_HIGH, vibration enabled, heads-up)
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            // Channel 1: Silent ongoing foreground tracking channel
            val trackingChannel = NotificationChannel(
                TRACKING_CHANNEL_ID,
                "WhatSpent Active Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays real-time WhatsApp conversation tracking status and daily usage summary"
                setShowBadge(false)
                enableVibration(false)
                enableLights(false)
            }

            // Channel 2: High importance alerts channel for daily chat limits
            val alertsChannel = NotificationChannel(
                ALERTS_CHANNEL_ID,
                "Daily Chat Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when reaching daily screen time limits on WhatsApp conversations"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(trackingChannel, alertsChannel))
        }
    }

    /**
     * Builds the persistent foreground tracking notification showing today's total WhatsApp
     * screen time and the top chat.
     *
     * Format: "Today: Xh Ym | Top: [Chat Name] (Zh Wm)"
     * If no chats tracked yet today: "Today: 0m | Top: None (0m)"
     */
    fun buildForegroundNotification(context: Context, state: ActiveSessionState): Notification {
        val todayStr = TimeFormatUtils.formatDurationShort(state.todayTotalMs)
        val topStr = if (!state.topChatName.isNullOrBlank() && state.topChatDurationMs > 0L) {
            "${state.topChatName} (${TimeFormatUtils.formatDurationShort(state.topChatDurationMs)})"
        } else {
            "None (0m)"
        }
        val contentText = "Today: $todayStr | Top: $topStr"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setContentTitle("WhatSpent Active")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification_pulse)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        // If actively tracking a conversation, show the active chat as subtitle
        if (state.isTracking && !state.currentDisplayName.isNullOrBlank()) {
            builder.setSubText("Active: ${state.currentDisplayName}")
        }

        return builder.build()
    }

    /**
     * Safely updates the ongoing foreground notification with the latest ActiveSessionState
     * after checking that notification permissions are granted.
     */
    fun updateForegroundNotification(context: Context, state: ActiveSessionState) {
        if (!PermissionHelper.isNotificationPermissionGranted(context)) return

        val notification = buildForegroundNotification(context, state)
        try {
            NotificationManagerCompat.from(context).notify(FOREGROUND_NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Handled safely in case permission was revoked during runtime
        }
    }

    /**
     * Fires a gentle heads-up notification when a chat reaches or exceeds its configured daily limit.
     */
    fun sendDailyLimitAlert(
        context: Context,
        chatDisplayName: String,
        limitMinutes: Int,
        currentMinutes: Int
    ) {
        if (!PermissionHelper.isNotificationPermissionGranted(context)) return

        val notificationId = chatDisplayName.hashCode() + 2000
        val title = "Daily Limit Reached"
        val content = "You've reached your daily limit of $limitMinutes mins for $chatDisplayName"
        val bigMessage = "You've spent $currentMinutes mins in $chatDisplayName today, reaching your limit of $limitMinutes mins. Consider taking a break!"

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_pulse)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigMessage))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handled safely
        }
    }
}
