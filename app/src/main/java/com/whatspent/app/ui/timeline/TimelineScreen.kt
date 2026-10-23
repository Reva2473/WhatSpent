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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatspent.app.data.model.TimelineSessionItem
import com.whatspent.app.data.model.formatDuration
import com.whatspent.app.ui.theme.getChatColor
import com.whatspent.app.util.TimeFormatUtils

// Explicit Design System Palette
private val ObsidianBg = Color(0xFF0B0B0D)
private val CharcoalBento = Color(0xFF1F1F21)
private val DarkCircleBtn = Color(0xFF262522)
private val ButterGold = Color(0xFFEFE29B)
private val SageMintCard = Color(0xFFBCE0D0)
private val WarmSandCard = Color(0xFFC9BA9B)
private val SoftOliveCard = Color(0xFFCED2BE)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Timeline",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Date Strip with Butter-Gold Active Pill
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(availableDates) { index, date ->
                    val isSelected = date == selectedDate
                    val label = when (date) {
                        TimeFormatUtils.todayDateString() -> "Today"
                        TimeFormatUtils.yesterdayDateString() -> "Yesterday"
                        else -> TimeFormatUtils.formatReadableDate(date)
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelectDate(date) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) ButterGold else CharcoalBento,
                        border = if (!isSelected) BorderStroke(1.dp, Color(0x1AFFFFFF)) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF141413) else TextMutedLight
                            )
                        }
                    }
                }
            }

            // Daily Summary Hero Card (SageMintCard squircle)
            DailySummaryHeroCard(
                date = selectedDate,
                totalDurationMs = totalDateDurationMs,
                sessionCount = sessionCount,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CHRONOLOGICAL SESSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMutedLight,
                    letterSpacing = 1.2.sp
                )
                if (sessionCount > 0) {
                    Text(
                        text = "$sessionCount recorded",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMutedLight
                    )
                }
            }

            // Chronological Session List
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = CharcoalBento,
                        border = BorderStroke(1.dp, Color(0x1FFFFFFF))
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(DarkCircleBtn),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(26.dp),
                                    tint = ButterGold
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "No Sessions Recorded",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No conversation activity was tracked on this date.",
                                fontSize = 13.sp,
                                color = TextMutedLight,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = sessions,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        PastelSessionPill(
                            item = item,
                            backgroundColor = PastelPills[index % PastelPills.size],
                            onClick = { onNavigateToChatDetail(item.chatName) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

/**
 * 32.dp squircle Hero Card styled with SageMintCard and dark ink typography.
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
                        text = "TOTAL TIME ON DAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMutedDark,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatDuration(totalDurationMs),
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
                            imageVector = Icons.Default.Schedule,
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

            Text(
                text = TimeFormatUtils.formatReadableDate(date),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMutedDark
            )
        }
    }
}

/**
 * Horizontal pastel pill session row with dark ink text and crisp white duration badge.
 */
@Composable
private fun PastelSessionPill(
    item: TimelineSessionItem,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val chatColor = getChatColor(item.chatName, item.colorTag)
    val timeRangeStr = TimeFormatUtils.formatTimeRange(item.startTime, item.endTime)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, Color(0x12000000))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Colored ring dot indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f)),
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF141615),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = timeRangeStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF4A4E4B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Crisp white right-hand value badge
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = TimeFormatUtils.formatDurationShort(item.durationMs),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF141615)
                )
            }
        }
    }
}
