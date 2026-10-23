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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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

// Explicit Design System Palette
private val ObsidianBg = Color(0xFF0B0B0D)
private val CharcoalBento = Color(0xFF1F1F21)
private val DarkCircleBtn = Color(0xFF262522)
private val ButterGold = Color(0xFFEFE29B)
private val SageMintCard = Color(0xFFBCE0D0)
private val SoftSageListCard = Color(0xFFCBD1C4)
private val PastelPills = listOf(
    Color(0xFFDCE2F9), // Lavender
    Color(0xFFCBEAF6), // Sky
    Color(0xFFC6F0DC), // Mint
    Color(0xFFF7F2CD)  // Cream
)
private val MintPositiveBadge = Color(0xFF8BE8B9)
private val CoralAccent = Color(0xFFFF6464)
private val TextDarkInk = Color(0xFF121413)
private val TextMutedDark = Color(0xFF4A4D4A)
private val TextLight = Color(0xFFF5F5F7)
private val TextMutedLight = Color(0xFF9E9B8E)

@OptIn(ExperimentalMaterial3Api::class)
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

    // Runtime permission launcher for notifications
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        letterSpacing = (-0.3).sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ObsidianBg
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // (01) Permissions Health Section
            item(key = "permissions_health_section") {
                SectionHeader(title = "(01) Permissions Health")
                Spacer(modifier = Modifier.height(10.dp))

                // Permissions Card in SoftSageListCard with 4 pastel pill rows
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = SoftSageListCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Local Monitoring System",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDarkInk
                            )
                            Text(
                                text = "WhatSpent processes all active chats directly on your device.",
                                fontSize = 12.sp,
                                color = TextMutedDark
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Row 1: Accessibility Tracker Service
                        PastelPermissionPillRow(
                            title = "Accessibility Tracker",
                            description = "Detects conversation opens locally",
                            icon = Icons.Default.Security,
                            isGranted = permissionState.isAccessibilityEnabled,
                            backgroundColor = PastelPills[0],
                            onGrantClick = {
                                context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
                            }
                        )

                        // Row 2: Notifications
                        PastelPermissionPillRow(
                            title = "Tracking Notifications",
                            description = "Foreground tracker & limit alerts",
                            icon = Icons.Default.Notifications,
                            isGranted = permissionState.isNotificationGranted,
                            backgroundColor = PastelPills[1],
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
                        PastelPermissionPillRow(
                            title = "Battery Optimization",
                            description = "Prevents OEM background termination",
                            icon = Icons.Default.BatteryAlert,
                            isGranted = permissionState.isBatteryOptimizationIgnored,
                            backgroundColor = PastelPills[2],
                            onGrantClick = {
                                context.startActivity(PermissionHelper.createBatteryOptimizationExemptionIntent(context))
                            }
                        )

                        // Row 4: Floating Overlay Pill
                        PastelPermissionPillRow(
                            title = "Floating Overlay Pill",
                            description = "Draggable live timer chip over chats",
                            icon = Icons.Default.Layers,
                            isGranted = permissionState.isOverlayGranted,
                            backgroundColor = PastelPills[3],
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
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = CharcoalBento,
                    border = BorderStroke(1.dp, Color(0x1FFFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                            color = Color(0x14FFFFFF)
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
                            color = Color(0x14FFFFFF)
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
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    color = CharcoalBento,
                    border = BorderStroke(1.dp, Color(0x1FFFFFFF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToOnboarding,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(22.dp),
                            border = BorderStroke(1.dp, Color(0x33FFFFFF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = ButterGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Re-open Permission Setup Wizard",
                                color = TextLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                onSeedDemoData()
                                Toast.makeText(context, "Sample analytics loaded!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ButterGold,
                                contentColor = Color(0xFF141413)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF141413)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Sample Analytics", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showClearDataDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralAccent,
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

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = CharcoalBento,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Clear All Tracking Data?",
                    fontWeight = FontWeight.Bold,
                    color = TextLight
                )
            },
            text = {
                Text(
                    "This will permanently delete all logged WhatsApp sessions and reset all conversation aliases and daily limits. This action cannot be undone.",
                    color = TextMutedLight
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
                        containerColor = CoralAccent,
                        contentColor = Color.White
                    )
                ) {
                    Text("Clear All Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = TextMutedLight)
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
        color = ButterGold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

/**
 * Pastel pill row inside SoftSageListCard.
 */
@Composable
private fun PastelPermissionPillRow(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    backgroundColor: Color,
    onGrantClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, Color(0x12000000))
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
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TextDarkInk
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = TextMutedDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MintPositiveBadge
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
                            tint = Color(0xFF0D2618)
                        )
                        Text(
                            text = "Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D2618)
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onGrantClick),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF141413)
                ) {
                    Text(
                        text = "Grant",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Switch row inside CharcoalBento card.
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
                    .size(38.dp)
                    .clip(CircleShape)
                .background(DarkCircleBtn),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = ButterGold
                )
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextLight
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = TextMutedLight
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF141413),
                checkedTrackColor = ButterGold,
                uncheckedTrackColor = Color(0xFF333336),
                uncheckedThumbColor = TextMutedLight
            )
        )
    }
}
