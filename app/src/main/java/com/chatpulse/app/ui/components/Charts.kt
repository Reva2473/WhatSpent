package com.chatpulse.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chatpulse.app.data.model.ChatSummary
import com.chatpulse.app.data.model.DailyUsagePoint
import com.chatpulse.app.data.model.formatDuration
import com.chatpulse.app.ui.theme.getChatColor
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Data representation of an individual donut arc segment.
 */
private data class ArcSegment(
    val summary: ChatSummary,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

/**
 * Animated Donut Chart displaying active conversation distribution with interactive tap detection,
 * center statistics, and a color legend.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedChatDonutChart(
    chatSummaries: List<ChatSummary>,
    totalDurationMs: Long,
    selectedChatName: String?,
    onChatSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    chartSize: Dp = 220.dp,
    strokeWidthNormal: Dp = 26.dp,
    strokeWidthSelected: Dp = 34.dp
) {
    val activeSummaries = remember(chatSummaries) {
        chatSummaries.filter { !it.isExcluded && it.durationMs > 0L }.take(7)
    }

    // Animation progress from 0f to 1f
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(activeSummaries) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // Compute arc segments
    val segments = remember(activeSummaries, totalDurationMs) {
        if (activeSummaries.isEmpty() || totalDurationMs <= 0L) {
            emptyList()
        } else {
            val list = mutableListOf<ArcSegment>()
            var currentAngle = -90f // Start from top
            val gapAngle = if (activeSummaries.size > 1) 3f else 0f
            val totalUsableSweep = 360f - (gapAngle * activeSummaries.size)

            val sumDuration = activeSummaries.sumOf { it.durationMs }.coerceAtLeast(1L)
            for (chat in activeSummaries) {
                val ratio = (chat.durationMs.toDouble() / sumDuration.toDouble()).toFloat()
                val sweep = (ratio * totalUsableSweep).coerceAtLeast(4f)
                val color = getChatColor(chat.chatName, chat.colorTag)
                list.add(ArcSegment(chat, color, currentAngle, sweep))
                currentAngle += sweep + gapAngle
            }
            list
        }
    }

    val selectedChat = remember(selectedChatName, chatSummaries) {
        chatSummaries.firstOrNull { it.chatName == selectedChatName }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(chartSize)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val emptyTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(segments) {
                        detectTapGestures { tapOffset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val dx = tapOffset.x - center.x
                            val dy = tapOffset.y - center.y
                            val distance = sqrt(dx * dx + dy * dy)
                            val innerRadius = (size.width / 2f) - strokeWidthSelected.toPx()
                            val outerRadius = (size.width / 2f)

                            if (distance in (innerRadius * 0.7f)..outerRadius) {
                                // Compute angle in degrees from -180 to 180
                                var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                // Normalize angle to 0..360 where -90 is 0
                                angle = (angle + 90f + 360f) % 360f

                                val clickedSegment = segments.firstOrNull { seg ->
                                    val segStart = (seg.startAngle + 90f + 360f) % 360f
                                    val segEnd = segStart + seg.sweepAngle
                                    if (segEnd <= 360f) {
                                        angle in segStart..segEnd
                                    } else {
                                        angle >= segStart || angle <= (segEnd % 360f)
                                    }
                                }

                                if (clickedSegment != null) {
                                    if (selectedChatName == clickedSegment.summary.chatName) {
                                        onChatSelected(null)
                                    } else {
                                        onChatSelected(clickedSegment.summary.chatName)
                                    }
                                } else {
                                    onChatSelected(null)
                                }
                            } else {
                                onChatSelected(null)
                            }
                        }
                    }
            ) {
                val strokeNormalPx = strokeWidthNormal.toPx()
                val strokeSelectedPx = strokeWidthSelected.toPx()
                val arcSize = size.width - strokeSelectedPx
                val topLeft = Offset(strokeSelectedPx / 2f, strokeSelectedPx / 2f)

                if (segments.isEmpty()) {
                    // Draw empty placeholder ring
                    drawArc(
                        color = emptyTrackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeNormalPx, cap = StrokeCap.Round)
                    )
                } else {
                    val progress = animationProgress.value

                    segments.forEach { segment ->
                        val isSelected = selectedChatName == segment.summary.chatName
                        val currentStroke = if (isSelected) strokeSelectedPx else strokeNormalPx
                        val currentSweep = segment.sweepAngle * progress

                        drawArc(
                            color = segment.color,
                            startAngle = segment.startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(arcSize, arcSize),
                            style = Stroke(
                                width = currentStroke,
                                cap = StrokeCap.Round
                            )
                        )
                    }
                }
            }

            // Center Callout Information
            Column(
                modifier = Modifier
                    .fillMaxSize(0.65f)
                    .clip(CircleShape)
                    .clickable { onChatSelected(null) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (selectedChat != null) {
                    Text(
                        text = selectedChat.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${selectedChat.percentage.roundToInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = getChatColor(selectedChat.chatName, selectedChat.colorTag)
                    )
                    Text(
                        text = selectedChat.formattedDuration,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Today's Total",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatDuration(totalDurationMs),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (activeSummaries.isNotEmpty()) "${activeSummaries.size} active chats" else "No chats yet",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        // Color Legend Below Donut
        if (segments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                segments.forEach { segment ->
                    val isSelected = selectedChatName == segment.summary.chatName
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (isSelected) onChatSelected(null)
                                else onChatSelected(segment.summary.chatName)
                            },
                        color = if (isSelected) {
                            segment.color.copy(alpha = 0.2f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(segment.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = segment.summary.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${segment.summary.percentage.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 7-Day Interactive Bar Chart featuring spring-animated vertical bars with rounded top corners,
 * an average usage dashed reference line, and tap-to-inspect tooltips.
 */
@Composable
fun SevenDayUsageBarChart(
    points: List<DailyUsagePoint>,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 190.dp
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(chartHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No activity in the last 7 days",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    // Spring animation for bar heights
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    val maxDurationMs = remember(points) {
        val maxVal = points.maxOfOrNull { it.totalDurationMs } ?: 0L
        if (maxVal <= 0L) 60_000L else maxVal // minimum 1 minute scale
    }

    val avgDurationMs = remember(points) {
        if (points.isNotEmpty()) points.map { it.totalDurationMs }.average().toLong() else 0L
    }

    val selectedPoint = selectedPointIndex?.let { points.getOrNull(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Selected Date Tooltip Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedPoint != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${selectedPoint.dayLabel} (${selectedPoint.date}): ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedPoint.formattedDuration,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                }
            } else {
                Text(
                    text = "Tap a bar to inspect daily time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (avgDurationMs > 0L) {
                Text(
                    text = "Avg: ${formatDuration(avgDurationMs)}/day",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bar Chart Canvas
        val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        val barBackgroundTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(points) {
                        detectTapGestures { tapOffset ->
                            val barSlotWidth = size.width / points.size
                            val tappedIndex = (tapOffset.x / barSlotWidth).toInt()
                            if (tappedIndex in points.indices) {
                                selectedPointIndex = if (selectedPointIndex == tappedIndex) null else tappedIndex
                            } else {
                                selectedPointIndex = null
                            }
                        }
                    }
            ) {
                val availableHeight = size.height - 30.dp.toPx() // Reserve 30dp at bottom for labels
                val count = points.size
                val slotWidth = size.width / count
                val barWidth = (slotWidth * 0.52f).coerceIn(12.dp.toPx(), 36.dp.toPx())
                val progress = animationProgress.value

                // Draw Average dashed reference line
                if (avgDurationMs > 0L && maxDurationMs > 0L) {
                    val avgRatio = (avgDurationMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
                    val avgY = availableHeight * (1f - avgRatio)

                    drawLine(
                        color = gridLineColor,
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                // Draw Bars
                points.forEachIndexed { index, point ->
                    val isSelected = selectedPointIndex == index
                    val isToday = index == points.size - 1

                    val centerX = (index * slotWidth) + (slotWidth / 2f)
                    val left = centerX - (barWidth / 2f)

                    // Background bar track
                    drawRoundRect(
                        color = barBackgroundTrackColor,
                        topLeft = Offset(left, 0f),
                        size = Size(barWidth, availableHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Foreground active bar
                    val ratio = (point.totalDurationMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
                    val barHeight = (availableHeight * ratio * progress).coerceAtLeast(if (point.totalDurationMs > 0L) 6.dp.toPx() else 0f)
                    val top = availableHeight - barHeight

                    val barColor = when {
                        isSelected -> highlightColor
                        isToday -> highlightColor.copy(alpha = 0.9f)
                        else -> highlightColor.copy(alpha = 0.65f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            // Day Labels Row at Bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(28.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                points.forEachIndexed { index, point ->
                    val isSelected = selectedPointIndex == index
                    val isToday = index == points.size - 1

                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = when {
                            isSelected -> highlightColor
                            isToday -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
