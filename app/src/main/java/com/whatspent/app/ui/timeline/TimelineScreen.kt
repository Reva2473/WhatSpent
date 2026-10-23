package com.whatspent.app.ui.timeline

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatspent.app.data.model.TimelineSessionItem
import com.whatspent.app.ui.theme.getChatColor
import com.whatspent.app.util.TimeFormatUtils

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
fun TimelineScreen(
    selectedDate: String,
    availableDates: List<String>,
    sessions: List<TimelineSessionItem>,
    onSelectDate: (String) -> Unit,
    onNavigateToChatDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDateDurationMs = remember(sessions) { sessions.sumOf { it.durationMs } }
    val sessionCount = sessions.size

    // Root container eliminating top & bottom empty spaces (no Scaffold / TopAppBar)
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
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = TextSubtle,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "Timeline",
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                letterSpacing = (-0.3).sp
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleBtnDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Sessions",
                    tint = TextSubtle,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Horizontal Date Pill Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(availableDates) { _, date ->
                val isSelected = date == selectedDate
                val label = when (date) {
                    TimeFormatUtils.todayDateString() -> "Today"
                    TimeFormatUtils.yesterdayDateString() -> "Yesterday"
                    else -> TimeFormatUtils.formatReadableDate(date)
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onSelectDate(date) },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) AcidLime else SurfaceCharcoal,
                    border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) OnAcidLimeInk else TextSubtle
                        )
                    }
                }
            }
        }

        // Main Content List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Daily Summary Hero Section
            item(key = "daily_summary_hero") {
                DailySummaryHeroCard(
                    date = selectedDate,
                    totalDurationMs = totalDateDurationMs,
                    sessionCount = sessionCount
                )
            }

            // Chronological Section Header
            item(key = "chronological_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CHRONOLOGICAL SESSIONS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubtle,
                        letterSpacing = 1.2.sp
                    )
                    if (sessionCount > 0) {
                        Text(
                            text = "$sessionCount recorded",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSubtle
                        )
                    }
                }
            }

            // Session List Items or Empty State
            if (sessions.isEmpty()) {
                item(key = "empty_sessions") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = SurfaceCharcoal,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(CircleBtnDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = AcidLime
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Sessions Recorded",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No conversation activity was tracked on this date.",
                                fontSize = 13.sp,
                                color = TextSubtle,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = sessions,
                    key = { _, item -> item.id }
                ) { _, item ->
                    DarkTimelineSessionRow(
                        item = item,
                        onClick = { onNavigateToChatDetail(item.chatName) }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Elevated Dark Daily Summary Hero Card with two-tone giant duration and Periwinkle/Lime pill.
 */
@Composable
private fun DailySummaryHeroCard(
    date: String,
    totalDurationMs: Long,
    sessionCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
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
                        text = "TOTAL TIME ON DAY",
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
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

            Text(
                text = TimeFormatUtils.formatReadableDate(date),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSubtle
            )
        }
    }
}

/**
 * Chronological Session Row matching the bottom list of the reference image.
 * 44.dp circular CircleBtnDark on the left with a colored dot/icon,
 * chat displayName in 16.sp SemiBold TextWhite and time range in 12.sp TextSubtle,
 * crisp two-tone duration display on the right.
 */
@Composable
private fun DarkTimelineSessionRow(
    item: TimelineSessionItem,
    onClick: () -> Unit
) {
    val chatColor = getChatColor(item.chatName, item.colorTag)
    val timeRangeStr = TimeFormatUtils.formatTimeRange(item.startTime, item.endTime)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCharcoal,
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
                // 44.dp circular icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CircleBtnDark),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(chatColor)
                    )
                }

                Column {
                    Text(
                        text = item.displayName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeRangeStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Crisp two-tone duration display on the right
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CircleBtnDark,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    TwoToneShortDuration(durationMs = item.durationMs)
                }
            }
        }
    }
}

/**
 * Two-tone giant duration display: primary number in TextWhite + secondary unit in TextMutedUnit.
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

/**
 * Two-tone compact short duration display.
 */
@Composable
private fun TwoToneShortDuration(
    durationMs: Long,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        when {
            hours > 0 -> {
                Text(
                    text = "${hours}h",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "${minutes}m",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMutedUnit
                )
            }
            minutes > 0 -> {
                Text(
                    text = "${minutes}m",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                if (seconds > 0) {
                    Text(
                        text = "${seconds}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMutedUnit
                    )
                }
            }
            else -> {
                Text(
                    text = "${seconds}s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
        }
    }
}
