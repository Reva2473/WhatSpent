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
import androidx.compose.foundation.layout.navigationBarsPadding
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

    // Root layout eliminating top/bottom empty space (no Scaffold / TopAppBar)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
    ) {
        // Main Scrollable Area
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Privacy Card in SurfaceCharcoal
            item(key = "onboarding_hero_banner") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // 100% On-Device Privacy Badge in AcidLime
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = AcidLime
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = OnAcidLimeInk
                                )
                                Text(
                                    text = "100% On-Device Privacy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnAcidLimeInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Welcome to WhatSpent",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Understand your WhatsApp screen time, track conversation focus, and set gentle daily limits. Never reads message text, contains zero tracking analytics, and requires no internet permission.",
                            fontSize = 13.5.sp,
                            color = TextSubtle,
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
                        color = AcidLime,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            // Step 01: Accessibility Service
            item(key = "step_01_accessibility") {
                NumberedStepDarkCard(
                    stepNumber = "01",
                    title = "Accessibility Tracker Service",
                    badge = "Required",
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
                NumberedStepDarkCard(
                    stepNumber = "02",
                    title = "Tracking & Limit Notifications",
                    badge = "Required",
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
                NumberedStepDarkCard(
                    stepNumber = "03",
                    title = "Battery Optimization Whitelist",
                    badge = "Recommended",
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
                NumberedStepDarkCard(
                    stepNumber = "04",
                    title = "Floating Overlay Pill",
                    badge = "Optional",
                    description = "Allows a draggable mini screen time timer to appear while actively chatting in WhatsApp.",
                    icon = Icons.Default.Layers,
                    isGranted = permissionStatus.isOverlayGranted,
                    buttonLabel = "Allow Overlay",
                    onAction = {
                        context.startActivity(PermissionHelper.createOverlayPermissionIntent(context))
                    }
                )
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Bottom Pinned Button with navigationBarsPadding()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = ObsidianBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = onCompleteOnboarding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcidLime,
                        contentColor = OnAcidLimeInk
                    )
                ) {
                    Text(
                        text = "Complete Setup & Enter WhatSpent",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Numbered Bento Step Card (01, 02, 03, 04) in SurfaceCharcoal with Periwinkle step badge and AcidLime action button.
 */
@Composable
private fun NumberedStepDarkCard(
    stepNumber: String,
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    buttonLabel: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
                    // 36.dp circular CircleBtnDark with Periwinkle step number
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CircleBtnDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stepNumber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeriwinkleViolet
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isGranted) PeriwinkleViolet.copy(alpha = 0.16f) else CircleBtnDark,
                    border = BorderStroke(
                        1.dp,
                        if (isGranted) PeriwinkleViolet.copy(alpha = 0.35f) else BorderSubtle
                    )
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
                                tint = PeriwinkleViolet
                            )
                        }
                        Text(
                            text = if (isGranted) "Granted" else badge,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) PeriwinkleViolet else TextSubtle
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                fontSize = 12.5.sp,
                color = TextSubtle,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AcidLime,
                        contentColor = OnAcidLimeInk
                    ),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnAcidLimeInk
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
