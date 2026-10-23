package com.whatspent.app.ui.settings

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.whatspent.app.ui.theme.LivePulseGreen
import com.whatspent.app.util.BiometricAuthHelper
import com.whatspent.app.util.PermissionHelper
import com.whatspent.app.util.PermissionStatusState

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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Health",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            // 1. Service & Permissions Health Section
            item(key = "permissions_health_header") {
                SectionHeader(title = "Service & Permissions Health")
            }

            item(key = "accessibility_health_card") {
                PermissionHealthCard(
                    title = "Accessibility Tracker Service",
                    description = "Required to detect when WhatsApp conversations open and close locally.",
                    isGranted = permissionState.isAccessibilityEnabled,
                    icon = Icons.Default.Security,
                    onGrantClick = {
                        context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
                    }
                )
            }

            item(key = "notification_health_card") {
                PermissionHealthCard(
                    title = "Tracking & Limit Notifications",
                    description = "Shows live tracking status and alerts you when conversation limits are reached.",
                    isGranted = permissionState.isNotificationGranted,
                    icon = Icons.Default.Notifications,
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
            }

            item(key = "overlay_health_card") {
                PermissionHealthCard(
                    title = "Floating Overlay Pill",
                    description = "Required to display the live timer chip over WhatsApp conversations.",
                    isGranted = permissionState.isOverlayGranted,
                    icon = Icons.Default.Layers,
                    onGrantClick = {
                        context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
                    }
                )
            }

            item(key = "battery_health_card") {
                PermissionHealthCard(
                    title = "Battery Optimization Whitelist",
                    description = "Prevents aggressive Android battery killers from ending WhatsApp tracking.",
                    isGranted = permissionState.isBatteryOptimizationIgnored,
                    icon = Icons.Default.BatteryAlert,
                    onGrantClick = {
                        context.startActivity(PermissionHelper.createBatteryOptimizationExemptionIntent(context))
                    }
                )
            }

            // 2. Features & Privacy Section
            item(key = "features_privacy_header") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(title = "Features & Privacy")
            }

            item(key = "floating_pill_switch_card") {
                SettingSwitchCard(
                    title = "Floating Overlay Pill",
                    description = "Show a compact, draggable live screen time chip over active WhatsApp chats.",
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
            }

            item(key = "biometric_lock_switch_card") {
                SettingSwitchCard(
                    title = "Biometric App Lock",
                    description = "Require fingerprint, face, or device PIN to access WhatSpent screen time records.",
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
            }

            item(key = "dynamic_color_switch_card") {
                SettingSwitchCard(
                    title = "Material You Dynamic Colors",
                    description = "Harmonize app colors with your Android system wallpaper theme.",
                    icon = Icons.Default.Palette,
                    checked = dynamicColorEnabled,
                    onCheckedChange = onToggleDynamicColor
                )
            }

            // 3. Data & Storage Management Section
            item(key = "data_management_header") {
                Spacer(modifier = Modifier.height(4.dp))
                SectionHeader(title = "Data Management")
            }

            item(key = "data_management_card") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToOnboarding,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Re-open Permission Setup Wizard")
                        }

                        FilledTonalButton(
                            onClick = {
                                onSeedDemoData()
                                Toast.makeText(context, "Sample analytics loaded!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Sample Analytics Data")
                        }

                        Button(
                            onClick = { showClearDataDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Tracking Data")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Tracking Data?") },
            text = {
                Text(
                    "This will permanently delete all logged WhatsApp chat sessions and reset all conversation aliases and daily limits. This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataDialog = false
                        Toast.makeText(context, "All tracking data cleared.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun PermissionHealthCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: ImageVector,
    onGrantClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(
                            if (isGranted) LivePulseGreen.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.errorContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isGranted) LivePulseGreen else MaterialTheme.colorScheme.error
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isGranted) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LivePulseGreen.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            modifier = Modifier.size(14.dp),
                            tint = LivePulseGreen
                        )
                        Text(
                            text = "Ready",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LivePulseGreen
                        )
                    }
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Grant",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchCard(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
