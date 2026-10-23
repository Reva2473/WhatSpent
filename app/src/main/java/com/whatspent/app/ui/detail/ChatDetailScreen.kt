package com.whatspent.app.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatspent.app.data.model.ChatDetailUiState
import com.whatspent.app.data.model.DetailRange
import com.whatspent.app.data.model.formatDuration
import com.whatspent.app.ui.components.SevenDayUsageBarChart
import com.whatspent.app.ui.theme.ChatChartPalette
import com.whatspent.app.ui.theme.getChatColor
import kotlin.math.roundToInt

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
fun ChatDetailScreen(
    uiState: ChatDetailUiState,
    onBackClick: () -> Unit,
    onSelectRange: (DetailRange) -> Unit,
    onUpdateDailyLimit: (Int?) -> Unit,
    onToggleExcluded: (Boolean) -> Unit,
    onUpdateAlias: (String?) -> Unit,
    onUpdateColorTag: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val chatColor = getChatColor(uiState.chatName, uiState.colorTag)
    var showAliasDialog by remember { mutableStateOf(false) }

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
            // Left 40.dp circular icon button (Back arrow)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleBtnDark)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite,
                    modifier = Modifier.size(19.dp)
                )
            }

            // Centered display name & optional original chat name
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            ) {
                Text(
                    text = uiState.displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (uiState.customAlias != null && uiState.customAlias != uiState.chatName) {
                    Text(
                        text = uiState.chatName,
                        fontSize = 11.sp,
                        color = TextSubtle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right 40.dp circular icon button (Edit Alias)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleBtnDark)
                    .clickable(onClick = { showAliasDialog = true }),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Custom Alias",
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
            // 1. Segmented Range Pill Bar (Today, 7 Days, 30 Days)
            item(key = "range_pill_bar") {
                SegmentedRangePillBar(
                    selectedRange = uiState.selectedRange,
                    onSelectRange = onSelectRange
                )
            }

            // 2. Hero Two-Tone Total Duration Card
            item(key = "hero_duration_card") {
                HeroTotalDurationCard(
                    rangeLabel = uiState.selectedRange.label,
                    totalDurationMs = uiState.totalDurationInRangeMs,
                    sessionCount = uiState.sessionCountInRange,
                    chatColor = chatColor
                )
            }

            // 3. Twin Dark Metric Cards (Average Session & Longest Session)
            item(key = "twin_metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TwinDarkMetricCard(
                        title = "Average Session",
                        durationMs = uiState.averageSessionMs,
                        badgeLabel = "Avg",
                        accentColor = PeriwinkleViolet,
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    TwinDarkMetricCard(
                        title = "Longest Session",
                        durationMs = uiState.longestSessionMs,
                        badgeLabel = "Peak",
                        accentColor = AcidLime,
                        icon = Icons.Default.HourglassBottom,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Historical Usage Chart Card
            item(key = "usage_chart_card") {
                HistoricalUsageChartCard(
                    points = uiState.dailyPoints,
                    rangeLabel = uiState.selectedRange.label
                )
            }

            // 5. Daily Screen Time Limit Card
            item(key = "daily_limit_card") {
                DailyScreenTimeLimitCard(
                    dailyLimitMinutes = uiState.dailyLimitMinutes,
                    todayDurationMs = uiState.todayDurationMs,
                    onUpdateLimit = onUpdateDailyLimit
                )
            }

            // 6. Customization & Privacy Card
            item(key = "customization_privacy_card") {
                CustomizationPrivacyCard(
                    customAlias = uiState.customAlias,
                    colorTag = uiState.colorTag,
                    isExcluded = uiState.isExcluded,
                    onOpenAliasDialog = { showAliasDialog = true },
                    onSelectColor = onUpdateColorTag,
                    onToggleExcluded = onToggleExcluded
                )
            }

            // Minimal bottom spacing
            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    // Custom Alias Edit Dialog
    if (showAliasDialog) {
        DarkCustomAliasDialog(
            currentAlias = uiState.customAlias ?: "",
            chatName = uiState.chatName,
            onDismiss = { showAliasDialog = false },
            onSave = { newAlias ->
                onUpdateAlias(newAlias)
                showAliasDialog = false
            }
        )
    }
}

/**
 * Segmented Range Pill Bar styled like [1d  1w  1m  6m  1y] from reference image.
 * A SurfaceCharcoal container with active pill in AcidLime.
 */
@Composable
private fun SegmentedRangePillBar(
    selectedRange: DetailRange,
    onSelectRange: (DetailRange) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DetailRange.values().forEach { range ->
                val isSelected = range == selectedRange
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onSelectRange(range) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) AcidLime else Color.Transparent
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAcidLimeInk else TextSubtle
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hero Total Duration Card in SurfaceCharcoal with two-tone giant duration display.
 */
@Composable
private fun HeroTotalDurationCard(
    rangeLabel: String,
    totalDurationMs: Long,
    sessionCount: Int,
    chatColor: Color
) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TOTAL SCREEN TIME (${rangeLabel.uppercase()})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TwoToneDuration(
                        durationMs = totalDurationMs,
                        primarySize = 36.sp,
                        secondarySize = 22.sp
                    )
                }

                // Session Count Pill Badge in Periwinkle Violet
                Surface(
                    shape = RoundedCornerShape(50),
                    color = PeriwinkleViolet.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, PeriwinkleViolet.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = PeriwinkleViolet
                        )
                        Text(
                            text = "$sessionCount ${if (sessionCount == 1) "session" else "sessions"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeriwinkleViolet
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(chatColor)
                )
                Text(
                    text = "Conversation focus & screen duration",
                    fontSize = 12.sp,
                    color = TextSubtle,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Twin Metric Bento Card in SurfaceCharcoal with two-tone duration.
 */
@Composable
private fun TwinDarkMetricCard(
    title: String,
    durationMs: Long,
    badgeLabel: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CircleBtnDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = accentColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = badgeLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TwoToneDuration(
                durationMs = durationMs,
                primarySize = 22.sp,
                secondarySize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSubtle
            )
        }
    }
}

/**
 * Historical Usage Chart Card in SurfaceCharcoal calling SevenDayUsageBarChart with PeriwinkleViolet.
 */
@Composable
private fun HistoricalUsageChartCard(
    points: List<com.whatspent.app.data.model.DailyUsagePoint>,
    rangeLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                Column {
                    Text(
                        text = "Usage Trend",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Daily conversation duration ($rangeLabel)",
                        fontSize = 12.sp,
                        color = TextSubtle
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CircleBtnDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = "Daily",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SevenDayUsageBarChart(
                points = points,
                highlightColor = PeriwinkleViolet
            )
        }
    }
}

/**
 * Daily Screen Time Limit Card with PeriwinkleViolet/AcidLime slider and preset pills.
 */
@Composable
private fun DailyScreenTimeLimitCard(
    dailyLimitMinutes: Int?,
    todayDurationMs: Long,
    onUpdateLimit: (Int?) -> Unit
) {
    val currentLimit = dailyLimitMinutes ?: 0
    var sliderValue by remember(dailyLimitMinutes) { mutableFloatStateOf(currentLimit.toFloat()) }
    val isOverLimit = currentLimit > 0 && todayDurationMs >= (currentLimit * 60_000L)
    val limitMs = (currentLimit * 60_000L).coerceAtLeast(1L)
    val progress = if (currentLimit > 0) (todayDurationMs.toFloat() / limitMs.toFloat()).coerceIn(0f, 1f) else 0f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Screen Time Budget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = if (currentLimit > 0) "$currentLimit minutes per day" else "No daily limit set (Off)",
                        fontSize = 12.sp,
                        color = if (isOverLimit) CoralAlert else TextSubtle,
                        fontWeight = if (isOverLimit) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (currentLimit > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOverLimit) CoralAlert else AcidLime
                    ) {
                        Text(
                            text = if (isOverLimit) "Exceeded" else "${(progress * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverLimit) Color.White else OnAcidLimeInk
                        )
                    }
                }
            }

            if (currentLimit > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isOverLimit) CoralAlert else AcidLime,
                    trackColor = BarInactiveCharcoal,
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Today: ${formatDuration(todayDurationMs)} of ${currentLimit}m limit",
                    fontSize = 12.sp,
                    color = TextSubtle
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider with PeriwinkleViolet thumb and track
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = (it / 5).roundToInt() * 5f },
                onValueChangeFinished = {
                    val finalLimit = sliderValue.toInt()
                    onUpdateLimit(if (finalLimit == 0) null else finalLimit)
                },
                valueRange = 0f..180f,
                steps = 35,
                colors = SliderDefaults.colors(
                    thumbColor = PeriwinkleViolet,
                    activeTrackColor = PeriwinkleViolet,
                    inactiveTrackColor = BarInactiveCharcoal
                )
            )

            // Preset pills: Off, 15m, 30m, 45m, 60m, 90m, 120m
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    0 to "Off",
                    15 to "15m",
                    30 to "30m",
                    45 to "45m",
                    60 to "60m",
                    90 to "90m",
                    120 to "120m"
                )
                items(presets) { (minutes, label) ->
                    val isSelected = currentLimit == minutes

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                sliderValue = minutes.toFloat()
                                onUpdateLimit(if (minutes == 0) null else minutes)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) AcidLime else CircleBtnDark,
                        border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAcidLimeInk else TextWhite
                        )
                    }
                }
            }
        }
    }
}

/**
 * Customization & Privacy Card in SurfaceCharcoal.
 */
@Composable
private fun CustomizationPrivacyCard(
    customAlias: String?,
    colorTag: Int?,
    isExcluded: Boolean,
    onOpenAliasDialog: () -> Unit,
    onSelectColor: (Int?) -> Unit,
    onToggleExcluded: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Customization & Privacy",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Display Alias Tile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CircleBtnDark)
                    .clickable(onClick = onOpenAliasDialog)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom Display Alias",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = customAlias ?: "None (Using original WhatsApp chat name)",
                        fontSize = 12.sp,
                        color = TextSubtle
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AcidLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = OnAcidLimeInk,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart Color Tag Picker Row
            Text(
                text = "Chart Color Accent",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ChatChartPalette) { color ->
                    val colorArgb = color.toArgb()
                    val isSelected = colorTag == colorArgb

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) AcidLime else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                onSelectColor(if (isSelected) null else colorArgb)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Exclude from Tracking Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exclude from Tracking",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Text(
                        text = "When enabled, WhatSpent ignores this conversation completely. Time is neither tracked nor saved.",
                        fontSize = 12.sp,
                        color = TextSubtle
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isExcluded,
                    onCheckedChange = onToggleExcluded,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnAcidLimeInk,
                        checkedTrackColor = AcidLime,
                        uncheckedTrackColor = BarInactiveCharcoal,
                        uncheckedThumbColor = TextSubtle
                    )
                )
            }
        }
    }
}

/**
 * Dark Custom Alias Dialog styled with SurfaceCharcoal container and AcidLime button.
 */
@Composable
private fun DarkCustomAliasDialog(
    currentAlias: String,
    chatName: String,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var text by remember { mutableStateOf(currentAlias) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceCharcoal,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Edit Chat Alias",
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        },
        text = {
            Column {
                Text(
                    text = "Original WhatsApp: $chatName",
                    fontSize = 12.sp,
                    color = TextSubtle
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Custom Display Name", color = TextSubtle) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = AcidLime,
                        unfocusedBorderColor = BorderSubtle
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.trim().ifBlank { null }) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcidLime,
                    contentColor = OnAcidLimeInk
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onSave(null) }) {
                Text("Reset", color = TextSubtle)
            }
        }
    )
}

/**
 * Two-tone duration display: primary number in TextWhite + secondary unit in TextMutedUnit.
 */
@Composable
private fun TwoToneDuration(
    durationMs: Long,
    modifier: Modifier = Modifier,
    primarySize: TextUnit = 36.sp,
    secondarySize: TextUnit = 22.sp,
    primaryColor: Color = TextWhite,
    secondaryColor: Color = TextMutedUnit
) {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when {
            hours > 0 -> {
                Text(
                    text = "${hours}h",
                    fontSize = primarySize,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "${minutes}m",
                    fontSize = secondarySize,
                    fontWeight = FontWeight.Bold,
                    color = secondaryColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            minutes > 0 -> {
                Text(
                    text = "${minutes}m",
                    fontSize = primarySize,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    letterSpacing = (-0.5).sp
                )
                if (seconds > 0) {
                    Text(
                        text = "${seconds}s",
                        fontSize = secondarySize,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            else -> {
                Text(
                    text = "${seconds}s",
                    fontSize = primarySize,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor,
                    letterSpacing = (-0.5).sp
                )
            }
        }
    }
}
