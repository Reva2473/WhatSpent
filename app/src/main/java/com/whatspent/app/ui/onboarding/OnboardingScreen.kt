package com.whatspent.app.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.whatspent.app.util.PermissionHelper

// Explicit Design System Palette
private val ObsidianBg = Color(0xFF0B0B0D)
private val CharcoalBento = Color(0xFF1F1F21)
private val DarkCircleBtn = Color(0xFF262522)
private val ButterGold = Color(0xFFEFE29B)
private val SageMintCard = Color(0xFFBCE0D0)
private val PastelPills = listOf(
    Color(0xFFDCE2F9), // Lavender
    Color(0xFFCBEAF6), // Sky
    Color(0xFFC6F0DC), // Mint
    Color(0xFFF7F2CD)  // Cream
)
private val MintPositiveBadge = Color(0xFF8BE8B9)
private val TextDarkInk = Color(0xFF121413)
private val TextMutedDark = Color(0xFF4A4D4A)
private val TextLight = Color(0xFFF5F5F7)
private val TextMutedLight = Color(0xFF9E9B8E)

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionStatus by remember { mutableStateOf(PermissionHelper.getPermissionStatus(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = PermissionHelper.getPermissionStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = PermissionHelper.getPermissionStatus(context)
        if (!isGranted && !PermissionHelper.isNotificationPermissionGranted(context)) {
            runCatching {
                context.startActivity(PermissionHelper.createNotificationSettingsIntent(context))
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionStatus = PermissionHelper.getPermissionStatus(context)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ObsidianBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Button(
                        onClick = onCompleteOnboarding,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ButterGold,
                            contentColor = Color(0xFF141413)
                        )
                    ) {
                        Text(
                            text = "Complete Setup & Enter WhatSpent",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Privacy Banner in SageMintCard (32.dp squircle corners)
            item(key = "onboarding_hero_banner") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = SageMintCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE8FCE2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextDarkInk
                                )
                                Text(
                                    text = "100% On-Device Privacy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Welcome to WhatSpent",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDarkInk,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Understand your WhatsApp screen time, track conversation focus, and set gentle daily limits. Never reads message text, contains zero tracking analytics, and requires no internet permission.",
                            fontSize = 13.5.sp,
                            color = TextMutedDark,
                            lineHeight = 19.sp
                        )
                    }
                }
            }

            item(key = "permission_steps_title") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PERMISSION SETUP GUIDE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ButterGold,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Step 01: Accessibility Service
            item(key = "step_01_accessibility") {
                NumberedStepBentoCard(
                    stepNumber = "01",
                    title = "Accessibility Tracker Service",
                    badge = "Required",
                    badgePillColor = PastelPills[0],
                    description = "Enables WhatSpent to detect when you are actively inside WhatsApp conversations to calculate real-time chat durations locally.",
                    icon = Icons.Default.Security,
                    isGranted = permissionStatus.isAccessibilityEnabled,
                    buttonLabel = "Enable Service",
                    onAction = {
                        context.startActivity(PermissionHelper.createAccessibilitySettingsIntent())
                    }
                )
            }

            // Step 02: Notifications
            item(key = "step_02_notifications") {
                NumberedStepBentoCard(
                    stepNumber = "02",
                    title = "Tracking & Limit Notifications",
                    badge = "Required",
                    badgePillColor = PastelPills[1],
                    description = "Required on Android 13+ to display the silent foreground tracker notification and alert you when conversation limits are reached.",
                    icon = Icons.Default.Notifications,
                    isGranted = permissionStatus.isNotificationGranted,
                    buttonLabel = "Grant Permission",
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            runCatching {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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

            // Step 03: Battery Optimization
            item(key = "step_03_battery") {
                NumberedStepBentoCard(
                    stepNumber = "03",
                    title = "Battery Optimization Whitelist",
                    badge = "Recommended",
                    badgePillColor = PastelPills[2],
                    description = "Prevents OEM aggressive memory killers from halting the accessibility tracker during long WhatsApp sessions.",
                    icon = Icons.Default.BatteryAlert,
                    isGranted = permissionStatus.isBatteryOptimizationIgnored,
                    buttonLabel = "Whitelist App",
                    onAction = {
                        context.startActivity(PermissionHelper.createBatteryOptimizationExemptionIntent(context))
                    }
                )
            }

            // Step 04: Floating Overlay Pill
            item(key = "step_04_overlay") {
                NumberedStepBentoCard(
                    stepNumber = "04",
                    title = "Floating Overlay Pill",
                    badge = "Optional",
                    badgePillColor = PastelPills[3],
                    description = "Allows a draggable mini screen time timer to appear while actively chatting in WhatsApp.",
                    icon = Icons.Default.Layers,
                    isGranted = permissionStatus.isOverlayGranted,
                    buttonLabel = "Allow Overlay",
                    onAction = {
                        context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Numbered Bento Step Card (01, 02, 03, 04) in CharcoalBento (28.dp squircle).
 */
@Composable
private fun NumberedStepBentoCard(
    stepNumber: String,
    title: String,
    badge: String,
    badgePillColor: Color,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CharcoalBento,
        border = BorderStroke(1.dp, Color(0x1FFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .background(DarkCircleBtn),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ButterGold
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isGranted) MintPositiveBadge else badgePillColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isGranted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF0D2618)
                            )
                        }
                        Text(
                            text = if (isGranted) "Granted" else badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) Color(0xFF0D2618) else TextDarkInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.5.sp,
                color = TextMutedLight,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ButterGold,
                        contentColor = Color(0xFF141413)
                    ),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF141413)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buttonLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
