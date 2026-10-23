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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatspent.app.data.model.ChatDetailUiState
import com.whatspent.app.data.model.DetailRange
import com.whatspent.app.data.model.formatDuration
import com.whatspent.app.ui.components.SevenDayUsageBarChart
import com.whatspent.app.ui.theme.ChatChartPalette
import com.whatspent.app.ui.theme.getChatColor
import kotlin.math.roundToInt

// Explicit Design System Palette
private val ObsidianBg = Color(0xFF0B0B0D)
private val CharcoalBento = Color(0xFF1F1F21)
private val DarkCircleBtn = Color(0xFF262522)
private val ButterGold = Color(0xFFEFE29B)
private val SageMintCard = Color(0xFFBCE0D0)
private val WarmSandCard = Color(0xFFC9BA9B)
private val SoftOliveCard = Color(0xFFCED2BE)
private val SoftSageListCard = Color(0xFFCBD1C4)
private val MintPositiveBadge = Color(0xFF8BE8B9)
private val CoralAccent = Color(0xFFFF6464)
private val TextDarkInk = Color(0xFF121413)
private val TextMutedDark = Color(0xFF4A4D4A)
private val TextLight = Color(0xFFF5F5F7)
private val TextMutedLight = Color(0xFF9E9B8E)

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.customAlias != null && uiState.customAlias != uiState.chatName) {
                            Text(
                                text = uiState.chatName,
                                fontSize = 11.sp,
                                color = TextMutedLight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkCircleBtn)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFD9D2B0),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ButterGold)
                            .clickable(onClick = { showAliasDialog = true }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Custom Alias",
                            tint = Color(0xFF141413),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Numbered Circular Badge Range Selector Strip
            item(key = "range_selector") {
                NumberedRangeSelectorStrip(
                    selectedRange = uiState.selectedRange,
                    onSelectRange = onSelectRange
                )
            }

            // 2. Hero Summary Card in SageMintCard (32.dp corners)
            item(key = "hero_summary_card") {
                HeroSummaryCard(
                    rangeLabel = uiState.selectedRange.label,
                    totalDuration = uiState.formattedTotalDuration,
                    sessionCount = uiState.sessionCountInRange,
                    chatColor = chatColor
                )
            }

            // 3. Twin Metric Bento Cards (Average Session & Longest Session)
            item(key = "twin_metrics") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TwinMetricBentoCard(
                        title = "Average Session",
                        value = uiState.formattedAverageSession,
                        badgeLabel = "Avg",
                        badgeColor = MintPositiveBadge,
                        badgeTextColor = Color(0xFF0D2618),
                        icon = Icons.Default.Timer,
                        modifier = Modifier.weight(1f)
                    )
                    TwinMetricBentoCard(
                        title = "Longest Session",
                        value = uiState.formattedLongestSession,
                        badgeLabel = "Peak",
                        badgeColor = CoralAccent,
                        badgeTextColor = Color.White,
                        icon = Icons.Default.HourglassBottom,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Historical Usage Chart Card in WarmSandCard (32.dp corners)
            item(key = "historical_chart") {
                WarmSandChartCard(
                    points = uiState.dailyPoints,
                    rangeLabel = uiState.selectedRange.label,
                    highlightColor = chatColor
                )
            }

            // 5. Daily Usage Limit Card in SoftOliveCard (32.dp corners)
            item(key = "daily_limit_card") {
                SoftOliveLimitCard(
                    dailyLimitMinutes = uiState.dailyLimitMinutes,
                    todayDurationMs = uiState.todayDurationMs,
                    onUpdateLimit = onUpdateDailyLimit
                )
            }

            // 6. Customization & Privacy Card in CharcoalBento (28.dp corners)
            item(key = "customization_card") {
                CustomizationBentoCard(
                    customAlias = uiState.customAlias,
                    colorTag = uiState.colorTag,
                    isExcluded = uiState.isExcluded,
                    onOpenAliasDialog = { showAliasDialog = true },
                    onSelectColor = onUpdateColorTag,
                    onToggleExcluded = onToggleExcluded
                )
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Custom Alias Edit Dialog
    if (showAliasDialog) {
        CustomAliasDialog(
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
 * Numbered circular badge strip: (01 Today, 02 7 Days, 03 30 Days)
 */
@Composable
private fun NumberedRangeSelectorStrip(
    selectedRange: DetailRange,
    onSelectRange: (DetailRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DetailRange.values().forEachIndexed { index, range ->
            val isSelected = range == selectedRange
            val numberStr = String.format("%02d", index + 1)

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onSelectRange(range) },
                shape = RoundedCornerShape(22.dp),
                color = if (isSelected) ButterGold else CharcoalBento,
                border = if (!isSelected) BorderStroke(1.dp, Color(0x1FFFFFFF)) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Small circular badge
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color(0xFF141413) else Color(0xFF2E2E32)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = numberStr,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ButterGold else TextMutedLight
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = range.label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF141413) else TextMutedLight
                    )
                }
            }
        }
    }
}

/**
 * Hero summary card in SageMintCard (32.dp squircle corners).
 */
@Composable
private fun HeroSummaryCard(
    rangeLabel: String,
    totalDuration: String,
    sessionCount: Int,
    chatColor: Color
) {
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
                        color = TextMutedDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = totalDuration,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDarkInk,
                        letterSpacing = (-0.5).sp
                    )
                }

                // Session Count Pill Badge in #E8FCE2
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8FCE2)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextDarkInk
                        )
                        Text(
                            text = "$sessionCount ${if (sessionCount == 1) "session" else "sessions"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDarkInk
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
                    color = TextMutedDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Twin metric card in CharcoalBento (26.dp corners).
 */
@Composable
private fun TwinMetricBentoCard(
    title: String,
    value: String,
    badgeLabel: String,
    badgeColor: Color,
    badgeTextColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = CharcoalBento,
        border = BorderStroke(1.dp, Color(0x1AFFFFFF))
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
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DarkCircleBtn),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = badgeColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextLight,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextMutedLight
            )
        }
    }
}

/**
 * Historical Usage Chart Card in WarmSandCard (32.dp squircle corners).
 */
@Composable
private fun WarmSandChartCard(
    points: List<com.whatspent.app.data.model.DailyUsagePoint>,
    rangeLabel: String,
    highlightColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = WarmSandCard
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
                Column {
                    Text(
                        text = "Usage Trend",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkInk
                    )
                    Text(
                        text = "Daily conversation duration ($rangeLabel)",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.4f)
                ) {
                    Text(
                        text = "Daily",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SevenDayUsageBarChart(
                points = points,
                highlightColor = Color(0xFF141413)
            )
        }
    }
}

/**
 * Daily Usage Limit Card in SoftOliveCard (32.dp squircle corners).
 */
@Composable
private fun SoftOliveLimitCard(
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
        shape = RoundedCornerShape(32.dp),
        color = SoftOliveCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Screen Time Budget",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDarkInk
                    )
                    Text(
                        text = if (currentLimit > 0) "$currentLimit minutes per day" else "No daily limit set (Off)",
                        fontSize = 12.sp,
                        color = if (isOverLimit) CoralAccent else TextMutedDark,
                        fontWeight = if (isOverLimit) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (currentLimit > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOverLimit) CoralAccent else Color.White
                    ) {
                        Text(
                            text = if (isOverLimit) "Exceeded" else "${(progress * 100).toInt()}%",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverLimit) Color.White else TextDarkInk
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
                    color = if (isOverLimit) CoralAccent else TextDarkInk,
                    trackColor = Color(0x33000000),
                    strokeCap = StrokeCap.Round
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Today: ${formatDuration(todayDurationMs)} of ${currentLimit}m limit",
                    fontSize = 12.sp,
                    color = TextMutedDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Slider with dark ink track/thumb
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
                    thumbColor = TextDarkInk,
                    activeTrackColor = TextDarkInk,
                    inactiveTrackColor = Color(0x33141413)
                )
            )

            // Crisp white / butter-gold preset pills
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
                        color = if (isSelected) ButterGold else Color.White.copy(alpha = 0.55f),
                        border = if (isSelected) BorderStroke(1.5.dp, TextDarkInk) else null
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = TextDarkInk
                        )
                    }
                }
            }
        }
    }
}

/**
 * Customization & Privacy Card in CharcoalBento (28.dp squircle corners).
 */
@Composable
private fun CustomizationBentoCard(
    customAlias: String?,
    colorTag: Int?,
    isExcluded: Boolean,
    onOpenAliasDialog: () -> Unit,
    onSelectColor: (Int?) -> Unit,
    onToggleExcluded: (Boolean) -> Unit
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
            Text(
                text = "Customization & Privacy",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Alias Tile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x14FFFFFF))
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
                        color = TextLight
                    )
                    Text(
                        text = customAlias ?: "None (Using original WhatsApp chat name)",
                        fontSize = 12.sp,
                        color = TextMutedLight
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ButterGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color(0xFF141413),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Color Tag Picker Row
            Text(
                text = "Chart Color Accent",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextLight
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
                                color = if (isSelected) ButterGold else Color.Transparent,
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
                        color = TextLight
                    )
                    Text(
                        text = "When enabled, WhatSpent ignores this conversation completely. Time is neither tracked nor saved.",
                        fontSize = 12.sp,
                        color = TextMutedLight
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isExcluded,
                    onCheckedChange = onToggleExcluded,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF141413),
                        checkedTrackColor = ButterGold,
                        uncheckedTrackColor = Color(0xFF333336),
                        uncheckedThumbColor = TextMutedLight
                    )
                )
            }
        }
    }
}

/**
 * Custom Alias Dialog styled with dark charcoal container and butter-gold button.
 */
@Composable
private fun CustomAliasDialog(
    currentAlias: String,
    chatName: String,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit
) {
    var text by remember { mutableStateOf(currentAlias) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CharcoalBento,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                text = "Edit Chat Alias",
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
        },
        text = {
            Column {
                Text(
                    text = "Original WhatsApp: $chatName",
                    fontSize = 12.sp,
                    color = TextMutedLight
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Custom Display Name", color = TextMutedLight) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight,
                        focusedBorderColor = ButterGold,
                        unfocusedBorderColor = Color(0x33FFFFFF)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.trim().ifBlank { null }) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButterGold,
                    contentColor = Color(0xFF141413)
                )
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { onSave(null) }) {
                Text("Reset", color = TextMutedLight)
            }
        }
    )
}
