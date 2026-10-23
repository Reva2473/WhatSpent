package com.whatspent.app.ui.settings

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.whatspent.app.util.BiometricAuthHelper
import com.whatspent.app.util.PermissionHelper

// Sleek Obsidian & Neon Acid-Lime Design Tokens
private val ObsidianBg = Color(0xFF0B0B0D)
private val SurfaceCharcoal = Color(0xFF17171A)
private val CircleBtnDark = Color(0xFF1C1C20)
private val BarInactiveCharcoal = Color(0xFF222227)
private val BorderSubtle = Color(0xFF28282D)
private val AcidLime = Color(0xFFE5FA6B)
private val OnAcidLimeInk = Color(0xFF0E0F0A)
private val PeriwinkleViolet = Color(0xFF9F8FEF)
private val TextWhite = Color(0xFFF6F6F9)
private val TextMutedUnit = Color(0xFF6E6E77)
private val TextSubtle = Color(0xFF8A8A93)
private val CoralAlert = Color(0xFFFF6B6B)

@Composable
fun SettingsScreen(
    floatingPillEnabled: Boolean,
    biometricLockEnabled: Boolean,
    dynamicColorEnabled: Boolean,
    onToggleFloatingPill: (Boolean) -> Unit,
    onToggleBiometricLock: (Boolean) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onSeedDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionState by remember { mutableStateOf(PermissionHelper.getPermissionStatus(context)) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionState = PermissionHelper.getPermissionStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Runtime permission launcher for notifications on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionState = PermissionHelper.getPermissionStatus(context)
        if (!isGranted && !PermissionHelper.isNotificationPermissionGranted(context)) {
            runCatching {
                context.startActivity(PermissionHelper.createNotificationSettingsIntent(context))
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionState = PermissionHelper.getPermissionStatus(context)
    }

    // Root layout eliminating top/bottom empty space (no Scaffold / TopAppBar)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Compact Custom Header Row (height 44.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleBtnDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Preferences",
                    tint = TextSubtle,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Settings",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                letterSpacing = (-0.3).sp
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleBtnDark)
                    .clickable(onClick = onNavigateToOnboarding),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Setup Wizard",
                    tint = AcidLime,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Scrollable Screen Content
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // (01) Permissions Health Section
            item(key = "permissions_health_section") {
                SectionHeader(title = "(01) Permissions Health")
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Local Monitoring System",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "WhatSpent processes all active chats directly on your device.",
                                fontSize = 12.sp,
                                color = TextSubtle
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Row 1: Accessibility Tracker Service
                        DarkPermissionRow(
                            title = "Accessibility Tracker",
                            description = "Detects conversation opens locally",
                            icon = Icons.Default.Security,
                            isGranted = permissionState.isAccessibilityEnabled,
                            onGrantClick = {
                                context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
                            }
                        )

                        // Row 2: Notifications
                        DarkPermissionRow(
                            title = "Tracking Notifications",
                            description = "Foreground tracker & limit alerts",
                            icon = Icons.Default.Notifications,
                            isGranted = permissionState.isNotificationGranted,
                            onGrantClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    runCatching {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }.onFailure {
                                        runCatching {
                                            context.startActivity(PermissionHelper.createNotificationSettingsIntent(context))
                                        }
                                    }
                                } else {
                                    runCatching {
                                        context.startActivity(PermissionHelper.createNotificationSettingsIntent(context))
                                    }
                                }
                            }
                        )

                        // Row 3: Battery Optimization Whitelist
                        DarkPermissionRow(
                            title = "Battery Optimization",
                            description = "Prevents OEM background termination",
                            icon = Icons.Default.BatteryAlert,
                            isGranted = permissionState.isBatteryOptimizationIgnored,
                            onGrantClick = {
                                context.startActivity(PermissionHelper.createBatteryOptimizationExemptionIntent(context))
                            }
                        )

                        // Row 4: Floating Overlay Pill
                        DarkPermissionRow(
                            title = "Floating Overlay Pill",
                            description = "Draggable live timer chip over chats",
                            icon = Icons.Default.Layers,
                            isGranted = permissionState.isOverlayGranted,
                            onGrantClick = {
                                context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
                            }
                        )
                    }
                }
            }

            // (02) Features & Privacy Section
            item(key = "features_privacy_section") {
                SectionHeader(title = "(02) Features & Privacy")
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FeatureSwitchRow(
                            title = "Floating Overlay Pill",
                            description = "Compact live screen time chip over active WhatsApp chats",
                            icon = Icons.Default.Visibility,
                            checked = floatingPillEnabled,
                            onCheckedChange = { desired ->
                                if (desired && !PermissionHelper.isOverlayPermissionGranted(context)) {
                                    Toast.makeText(
                                        context,
                                        "Please grant 'Display over other apps' first",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
                                } else {
                                    onToggleFloatingPill(desired)
                                }
                            }
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth().height(1.dp),
                            color = BorderSubtle
                        ) {}

                        FeatureSwitchRow(
                            title = "Biometric App Lock",
                            description = "Require fingerprint, face, or PIN to access WhatSpent",
                            icon = Icons.Default.Fingerprint,
                            checked = biometricLockEnabled,
                            onCheckedChange = { desired ->
                                if (activity != null && BiometricAuthHelper.canAuthenticate(context)) {
                                    BiometricAuthHelper.authenticate(
                                        activity = activity,
                                        title = if (desired) "Enable App Lock" else "Disable App Lock",
                                        subtitle = "Authenticate to update biometric protection",
                                        onSuccess = {
                                            onToggleBiometricLock(desired)
                                        },
                                        onError = { errorMsg ->
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Biometric authentication not configured on this device",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth().height(1.dp),
                            color = BorderSubtle
                        ) {}

                        FeatureSwitchRow(
                            title = "Material You Dynamic Colors",
                            description = "Harmonize accent colors with system theme",
                            icon = Icons.Default.Palette,
                            checked = dynamicColorEnabled,
                            onCheckedChange = onToggleDynamicColor
                        )
                    }
                }
            }

            // (03) Data Management Section
            item(key = "data_management_section") {
                SectionHeader(title = "(03) Data Management")
                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToOnboarding,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CircleBtnDark,
                                contentColor = TextWhite
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = TextWhite
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Re-open Permission Setup Wizard",
                                color = TextWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                onSeedDemoData()
                                Toast.makeText(context, "Sample analytics loaded!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AcidLime,
                                contentColor = OnAcidLimeInk
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = OnAcidLimeInk
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Sample Analytics", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showClearDataDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralAlert,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Tracking Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Minimal bottom spacer
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = SurfaceCharcoal,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Clear All Tracking Data?",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            },
            text = {
                Text(
                    "This will permanently delete all logged WhatsApp sessions and reset all conversation aliases and daily limits. This action cannot be undone.",
                    color = TextSubtle
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "All tracking data cleared.", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralAlert,
                        contentColor = Color.White
                    )
                ) {
                    Text("Clear All Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = TextSubtle)
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = AcidLime,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

/**
 * Clean dark permission row with 42.dp circular CircleBtnDark icon and AcidLime "Grant" pill or Periwinkle/Lime "Active" badge.
 */
@Composable
private fun DarkPermissionRow(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = CircleBtnDark,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceCharcoal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isGranted) PeriwinkleViolet else AcidLime
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = TextSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PeriwinkleViolet.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, PeriwinkleViolet.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            modifier = Modifier.size(13.dp),
                            tint = PeriwinkleViolet
                        )
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeriwinkleViolet
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onGrantClick),
                    shape = RoundedCornerShape(50),
                    color = AcidLime
                ) {
                    Text(
                        text = "Grant",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnAcidLimeInk
                    )
                }
            }
        }
    }
}

/**
 * Feature Switch row inside SurfaceCharcoal card.
 */
@Composable
private fun FeatureSwitchRow(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleBtnDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AcidLime
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextSubtle
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = OnAcidLimeInk,
                checkedTrackColor = AcidLime,
                uncheckedTrackColor = BarInactiveCharcoal,
                uncheckedThumbColor = TextSubtle
            )
        )
    }
}
