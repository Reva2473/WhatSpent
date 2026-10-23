package com.chatpulse.app.util

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Snapshot of all critical permissions required for ChatPulse operations.
 */
data class PermissionStatusState(
    val isAccessibilityEnabled: Boolean,
    val isNotificationGranted: Boolean,
    val isOverlayGranted: Boolean,
    val isBatteryOptimizationIgnored: Boolean
) {
    /**
     * Core tracking is considered ready when both Accessibility and Notification permissions are granted.
     */
    val isCoreTrackingReady: Boolean
        get() = isAccessibilityEnabled && isNotificationGranted
}

/**
 * Utility helper for checking and requesting system permissions for ChatPulse.
 */
object PermissionHelper {

    private const val TRACKER_SERVICE_CLASS_NAME = "com.chatpulse.app.service.WhatsAppTrackerService"

    /**
     * Inspects current state of all permissions and returns a unified state object.
     */
    fun getPermissionStatus(context: Context): PermissionStatusState {
        return PermissionStatusState(
            isAccessibilityEnabled = isAccessibilityServiceEnabled(context),
            isNotificationGranted = isNotificationPermissionGranted(context),
            isOverlayGranted = isOverlayPermissionGranted(context),
            isBatteryOptimizationIgnored = isIgnoringBatteryOptimizations(context)
        )
    }

    /**
     * Checks if ChatPulse's WhatsAppTrackerService is enabled in Android Accessibility settings.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = ComponentName(context, TRACKER_SERVICE_CLASS_NAME)
        val expectedShortName = expectedComponentName.flattenToShortString()
        val expectedLongName = expectedComponentName.flattenToString()

        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            if (componentNameString.equals(expectedShortName, ignoreCase = true) ||
                componentNameString.equals(expectedLongName, ignoreCase = true) ||
                (componentNameString.contains(context.packageName) &&
                    componentNameString.contains("WhatsAppTrackerService"))
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Checks whether notification permission is granted (API 33+ runtime permission or pre-33 system setting).
     */
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Checks if SYSTEM_ALERT_WINDOW (display over other apps) permission is granted.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Checks if the app is exempt from battery optimizations.
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Creates an Intent to navigate the user directly to the Accessibility settings screen.
     */
    fun createAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an Intent to prompt the user to allow overlay permission for ChatPulse.
     */
    fun createOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Creates an Intent to request battery optimization exemption for background tracking stability.
     */
    fun createBatteryOptimizationExemptionIntent(context: Context): Intent {
        return try {
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } catch (_: Exception) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
    }

    /**
     * Creates an Intent to open the notification configuration screen for ChatPulse.
     */
    fun createNotificationSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
