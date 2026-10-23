package com.whatspent.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatspent.app.data.model.ChatSummary
import com.whatspent.app.data.model.DailyUsagePoint
import com.whatspent.app.data.model.formatDuration
import com.whatspent.app.ui.theme.ButterGoldAccent
import com.whatspent.app.ui.theme.CapsuleGoldBar
import com.whatspent.app.ui.theme.CapsulePeachBar
import com.whatspent.app.ui.theme.CharcoalBentoSurface
import com.whatspent.app.ui.theme.ElevatedCharcoalSurface
import com.whatspent.app.ui.theme.MutedChampagneIcon
import com.whatspent.app.ui.theme.PastelCardInk
import com.whatspent.app.ui.theme.PastelCardMutedInk
import com.whatspent.app.ui.theme.SageMintInnerCallout
import com.whatspent.app.ui.theme.SoftOliveChartCard
import com.whatspent.app.ui.theme.WarmSandCalloutPill
import com.whatspent.app.ui.theme.WarmSandChartCard
import com.whatspent.app.ui.theme.getChatColor
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

// ============================================================================
// 1. SIGNATURE 180° SEMI-CIRCULAR GAUGE CHART ("Top 5 chats by spend")
// ============================================================================

/**
 * Data segment for an individual arch slice in the 180° gauge.
 */
private data class SemiCircleSegment(
    val summary: ChatSummary,
    val color: Color,
    val startAngle: Float,
    val sweepAngle: Float
)

/**
 * Animated 180° Semi-Circular Arch Gauge Chart matching the signature "Top 5 chats by spend"
 * card from the design system.
 *
 * - Draws a thick 28.dp arch from 180° to 360° across the top half with a subtle track (Color(0xFFA2C7B7)).
 * - Proportional multi-colored segments from ChatChartPalette separated by crisp 2° gaps.
 * - Tap gesture detection on segments to select/deselect a chat.
 * - Floating #E8FCE2 (SageMintInnerCallout) rounded pill badge in the center showing duration (18.sp bold)
 *   and chat name (12.sp #496156), with interactive pager dots underneath.
 * - Summary legend below the arch with color dots and percentages.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedSemiCircleGaugeChart(
    chatSummaries: List<ChatSummary>,
    totalDurationMs: Long,
    selectedChatName: String?,
    onChatSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    trackColor: Color = Color(0xFFA2C7B7),
    showLegend: Boolean = true
) {
    val activeSummaries = remember(chatSummaries) {
        chatSummaries.filter { !it.isExcluded && it.durationMs > 0L }.take(5)
    }

    val chatNamesKey = remember(activeSummaries) { activeSummaries.map { it.chatName } }
    val animationProgress = remember { Animatable(1f) }
    LaunchedEffect(chatNamesKey) {
        if (activeSummaries.isNotEmpty()) {
            animationProgress.snapTo(0.25f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    // Compute 180° proportional arch segments
    val segments = remember(activeSummaries, totalDurationMs) {
        if (activeSummaries.isEmpty() || totalDurationMs <= 0L) {
            emptyList()
        } else {
            val list = mutableListOf<SemiCircleSegment>()
            val gapAngle = if (activeSummaries.size > 1) 2f else 0f
            val totalGaps = gapAngle * (activeSummaries.size - 1)
            val usableSweep = 180f - totalGaps
            val sumDuration = activeSummaries.sumOf { it.durationMs }.coerceAtLeast(1L)

            var currentAngle = 180f
            for (chat in activeSummaries) {
                val ratio = (chat.durationMs.toDouble() / sumDuration.toDouble()).toFloat()
                val sweep = (ratio * usableSweep).coerceAtLeast(3f)
                val color = getChatColor(chat.chatName, chat.colorTag)
                list.add(SemiCircleSegment(chat, color, currentAngle, sweep))
                currentAngle += sweep + gapAngle
            }
            list
        }
    }

    // Currently inspected chat for inner callout pill
    val displayChat = remember(selectedChatName, activeSummaries) {
        if (selectedChatName != null) {
            activeSummaries.firstOrNull { it.chatName == selectedChatName }
                ?: chatSummaries.firstOrNull { it.chatName == selectedChatName }
                ?: activeSummaries.firstOrNull()
        } else {
            activeSummaries.firstOrNull()
        }
    }

    val activeDotIndex = remember(displayChat, activeSummaries) {
        if (displayChat != null) {
            activeSummaries.indexOfFirst { it.chatName == displayChat.chatName }.coerceAtLeast(0)
        } else 0
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Upper container with Canvas and nested floating callout pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(segments) {
                        detectTapGestures { tapOffset ->
                            val strokePx = strokeWidth.toPx()
                            val archDiameter = (size.width - strokePx - 16.dp.toPx()).coerceAtLeast(10f)
                            val radius = archDiameter / 2f
                            val centerX = size.width / 2f
                            val topY = strokePx / 2f + 4.dp.toPx()
                            val centerY = topY + radius

                            val dx = tapOffset.x - centerX
                            val dy = tapOffset.y - centerY
                            val distance = sqrt(dx * dx + dy * dy)
                            val innerLimit = radius - (strokePx * 0.75f)
                            val outerLimit = radius + (strokePx * 0.75f)

                            // Check if tap fell inside arch band in top half
                            if (distance in innerLimit..outerLimit && dy <= (strokePx * 0.4f)) {
                                var deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                if (deg < 0f) deg += 360f

                                val clicked = segments.firstOrNull { seg ->
                                    deg >= seg.startAngle && deg <= (seg.startAngle + seg.sweepAngle)
                                }

                                if (clicked != null) {
                                    if (selectedChatName == clicked.summary.chatName) {
                                        onChatSelected(null)
                                    } else {
                                        onChatSelected(clicked.summary.chatName)
                                    }
                                } else {
                                    onChatSelected(null)
                                }
                            }
                        }
                    }
            ) {
                val strokePx = strokeWidth.toPx()
                val archDiameter = (size.width - strokePx - 16.dp.toPx()).coerceAtLeast(10f)
                val radius = archDiameter / 2f
                val centerX = size.width / 2f
                val topY = strokePx / 2f + 4.dp.toPx()
                val topLeft = Offset(centerX - radius, topY)
                val arcSize = Size(archDiameter, archDiameter)

                // 1. Background semi-circular track (180° to 360°)
                drawArc(
                    color = trackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 2. Foreground proportional chat segments
                if (segments.isNotEmpty()) {
                    val progress = animationProgress.value
                    segments.forEach { seg ->
                        val isSelected = selectedChatName == seg.summary.chatName
                        val currentStroke = if (isSelected) strokePx + 4.dp.toPx() else strokePx
                        val currentSweep = seg.sweepAngle * progress

                        drawArc(
                            color = seg.color,
                            startAngle = seg.startAngle,
                            sweepAngle = currentSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = currentStroke,
                                cap = if (segments.size == 1) StrokeCap.Round else StrokeCap.Butt
                            )
                        )
                    }
                }
            }

            // Floating #E8FCE2 (SageMintInnerCallout) rounded pill badge in the lower arch center
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SageMintInnerCallout,
                    shadowElevation = 1.dp,
                    modifier = Modifier.clickable { onChatSelected(null) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayChat?.formattedDuration ?: "0m",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PastelCardInk,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = displayChat?.displayName ?: "No chats today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF496156),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive pager dots • • • underneath the callout pill
                if (activeSummaries.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeSummaries.indices.forEach { index ->
                            val isActive = index == activeDotIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isActive) 6.dp else 4.5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) PastelCardInk else PastelCardInk.copy(alpha = 0.25f)
                                    )
                                    .clickable {
                                        onChatSelected(activeSummaries[index].chatName)
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Summary legend below the arch
        if (showLegend && segments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(6.dp),
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
                            segment.color.copy(alpha = 0.25f)
                        } else {
                            Color(0xFF121413).copy(alpha = 0.08f)
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
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = PastelCardInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${segment.summary.percentage.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = PastelCardMutedInk
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Backward-compatible wrapper delegating to [AnimatedSemiCircleGaugeChart].
 * Preserves the exact signature so existing callers work seamlessly.
 */
@Composable
fun AnimatedChatDonutChart(
    chatSummaries: List<ChatSummary>,
    totalDurationMs: Long,
    selectedChatName: String?,
    onChatSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    chartSize: Dp = 220.dp,
    strokeWidthNormal: Dp = 28.dp,
    strokeWidthSelected: Dp = 34.dp
) {
    AnimatedSemiCircleGaugeChart(
        chatSummaries = chatSummaries,
        totalDurationMs = totalDurationMs,
        selectedChatName = selectedChatName,
        onChatSelected = onChatSelected,
        modifier = modifier,
        strokeWidth = strokeWidthNormal
    )
}

// ============================================================================
// 2. WARM SAND BEZIER TREND CHART (7-Day Trend Card)
// ============================================================================

/**
 * 7-Day Trend Chart designed for the #C9BA9B (WarmSandChartCard) surface.
 *
 * - Draws subtle horizontal gridlines (Color(0xFFB3A486)).
 * - Y-axis labels on the left, X-axis day labels at the bottom.
 * - Smooth cubic Bezier curve in #181715 (3.dp stroke) with circular node points.
 * - Floating #DAF5E6 (WarmSandCalloutPill) callout badge positioned above the selected or peak day point.
 */
@Composable
fun WarmSandBezierTrendChart(
    points: List<DailyUsagePoint>,
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = WarmSandChartCard,
    calloutPillColor: Color = WarmSandCalloutPill,
    lineColor: Color = Color(0xFF181715),
    gridColor: Color = Color(0xFFB3A486),
    chartHeight: Dp = 190.dp,
    selectedDayIndex: Int? = null,
    onDaySelected: ((Int?) -> Unit)? = null
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
                color = PastelCardMutedInk
            )
        }
        return
    }

    var internalSelectedDay by remember { mutableStateOf<Int?>(null) }
    val effectiveSelectedDay = selectedDayIndex ?: internalSelectedDay

    val datesKey = remember(points) { points.map { it.date } }
    val animationProgress = remember { Animatable(1f) }
    LaunchedEffect(datesKey) {
        animationProgress.snapTo(0.25f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val maxDurationMs = remember(points) {
        val maxVal = points.maxOfOrNull { it.totalDurationMs } ?: 0L
        if (maxVal <= 0L) 3600_000L else maxVal
    }

    val peakIndex = remember(points) {
        points.indices.maxByOrNull { points[it].totalDurationMs } ?: (points.size - 1)
    }
    val targetIndex = effectiveSelectedDay ?: peakIndex
    val targetPoint = points.getOrNull(targetIndex)

    // Layout reservations
    val leftPadding = 36.dp
    val rightPadding = 16.dp
    val topPadding = 34.dp
    val bottomPadding = 26.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        // Y-axis label column on left
        Column(
            modifier = Modifier
                .width(leftPadding)
                .fillMaxSize()
                .padding(top = topPadding - 8.dp, bottom = bottomPadding - 6.dp, start = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = formatDuration(maxDurationMs),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = PastelCardMutedInk
            )
            Text(
                text = formatDuration(maxDurationMs / 2),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = PastelCardMutedInk
            )
            Text(
                text = "0m",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = PastelCardMutedInk
            )
        }

        // Canvas for gridlines, cubic Bezier curve, and node points
        var badgeOffset by remember { mutableStateOf<Offset?>(null) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftPadding, end = rightPadding)
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val usableWidth = size.width
                        val stepX = if (points.size > 1) usableWidth / (points.size - 1) else usableWidth
                        val tappedIdx = ((tapOffset.x + (stepX / 2f)) / stepX).toInt().coerceIn(0, points.size - 1)
                        if (onDaySelected != null) {
                            onDaySelected(if (effectiveSelectedDay == tappedIdx) null else tappedIdx)
                        } else {
                            internalSelectedDay = if (internalSelectedDay == tappedIdx) null else tappedIdx
                        }
                    }
                }
        ) {
            val usableWidth = size.width
            val topY = topPadding.toPx()
            val usableHeight = size.height - topY - bottomPadding.toPx()
            val progress = animationProgress.value

            // 1. Horizontal dashed gridlines (100%, 50%, 0%)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            listOf(0f, 0.5f, 1f).forEach { ratio ->
                val gridY = topY + (usableHeight * ratio)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gridY),
                    end = Offset(usableWidth, gridY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // 2. Compute coordinates of 7 daily points
            val stepX = if (points.size > 1) usableWidth / (points.size - 1) else usableWidth
            val coords = points.mapIndexed { index, p ->
                val x = index * stepX
                val ratio = (p.totalDurationMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
                val y = (topY + usableHeight) - (ratio * usableHeight * progress)
                Offset(x, y)
            }

            // Update badge target coordinate
            if (targetIndex in coords.indices) {
                badgeOffset = coords[targetIndex]
            }

            if (coords.isNotEmpty()) {
                // 3. Construct smooth cubic Bezier curve path
                val curvePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 0 until coords.size - 1) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        cubicTo(
                            x1 = controlX, y1 = p0.y,
                            x2 = controlX, y2 = p1.y,
                            x3 = p1.x, y3 = p1.y
                        )
                    }
                }

                // 4. Subtle translucent gradient fill under the curve
                val fillPath = Path().apply {
                    addPath(curvePath)
                    lineTo(coords.last().x, topY + usableHeight)
                    lineTo(coords.first().x, topY + usableHeight)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.12f),
                            lineColor.copy(alpha = 0.01f)
                        ),
                        startY = topY,
                        endY = topY + usableHeight
                    )
                )

                // 5. Stroke the smooth cubic Bezier line (3.dp in #181715)
                drawPath(
                    path = curvePath,
                    color = lineColor,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 6. Draw circular node points (o)
                coords.forEachIndexed { i, pt ->
                    val isTarget = i == targetIndex
                    if (isTarget) {
                        drawCircle(
                            color = lineColor,
                            radius = 5.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = calloutPillColor,
                            radius = 3.2.dp.toPx(),
                            center = pt
                        )
                    } else {
                        drawCircle(
                            color = lineColor,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = cardBackgroundColor,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        // Floating #DAF5E6 (WarmSandCalloutPill) callout badge
        if (targetPoint != null && badgeOffset != null) {
            val offset = badgeOffset!!
            val density = LocalDensity.current
            val pillHalfWidthPx = with(density) { 42.dp.toPx() }
            val badgeLiftPx = with(density) { 28.dp.toPx() }
            val minTopPx = with(density) { 4.dp.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = leftPadding, end = rightPadding)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = calloutPillColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier.offset {
                        val clampedX = (offset.x - pillHalfWidthPx).coerceAtLeast(0f)
                        val clampedY = (offset.y - badgeLiftPx).coerceAtLeast(minTopPx)
                        IntOffset(clampedX.roundToInt(), clampedY.roundToInt())
                    }
                ) {
                    Text(
                        text = "${targetPoint.dayLabel}: ${targetPoint.formattedDuration}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCardInk,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // X-axis day labels row at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = leftPadding, end = rightPadding)
                .height(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            points.forEachIndexed { index, point ->
                val isTarget = index == targetIndex
                Text(
                    text = point.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Medium,
                    color = if (isTarget) PastelCardInk else PastelCardMutedInk,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// 3. SOFT OLIVE CAPSULE BAR CHART & SEVEN DAY USAGE BAR CHART
// ============================================================================

/**
 * Capsule Bar Chart styled with rounded vertical capsule bars in #F3B282 peach and #F7D486 butter-gold,
 * a connected o—o dark trend line overlay, and interactive tap-to-inspect callout pill.
 *
 * Compatible with both #CED2BE pastel bento cards and dark obsidian bento surfaces.
 */
@Composable
fun SoftOliveCapsuleBarChart(
    points: List<DailyUsagePoint>,
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = SoftOliveChartCard,
    isPastelCard: Boolean = true,
    chartHeight: Dp = 190.dp,
    selectedDayIndex: Int? = null,
    onDaySelected: ((Int?) -> Unit)? = null
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
                color = if (isPastelCard) PastelCardMutedInk else MutedChampagneIcon
            )
        }
        return
    }

    var internalSelectedDay by remember { mutableStateOf<Int?>(null) }
    val effectiveSelectedDay = selectedDayIndex ?: internalSelectedDay

    val datesKey = remember(points) { points.map { it.date } }
    val animationProgress = remember { Animatable(1f) }
    LaunchedEffect(datesKey) {
        animationProgress.snapTo(0.25f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    val maxDurationMs = remember(points) {
        val maxVal = points.maxOfOrNull { it.totalDurationMs } ?: 0L
        if (maxVal <= 0L) 3600_000L else maxVal
    }

    val peakIndex = remember(points) {
        points.indices.maxByOrNull { points[it].totalDurationMs } ?: (points.size - 1)
    }
    val targetIndex = effectiveSelectedDay ?: peakIndex
    val targetPoint = points.getOrNull(targetIndex)

    val trackColor = if (isPastelCard) Color(0xFFB5BAA5) else ElevatedCharcoalSurface
    val trendLineColor = if (isPastelCard) Color(0xFF181715) else ButterGoldAccent

    val topPadding = 32.dp
    val bottomPadding = 26.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        var calloutAnchor by remember { mutableStateOf<Offset?>(null) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val slotWidth = size.width / points.size
                        val tappedIdx = (tapOffset.x / slotWidth).toInt().coerceIn(0, points.size - 1)
                        if (onDaySelected != null) {
                            onDaySelected(if (effectiveSelectedDay == tappedIdx) null else tappedIdx)
                        } else {
                            internalSelectedDay = if (internalSelectedDay == tappedIdx) null else tappedIdx
                        }
                    }
                }
        ) {
            val count = points.size
            val slotWidth = size.width / count
            val barWidth = (slotWidth * 0.45f).coerceIn(12.dp.toPx(), 28.dp.toPx())
            val topY = topPadding.toPx()
            val availableHeight = size.height - topY - bottomPadding.toPx()
            val progress = animationProgress.value

            val nodePoints = mutableListOf<Offset>()

            // 1. Draw vertical capsule tracks and active pill bars
            points.forEachIndexed { i, p ->
                val centerX = (i * slotWidth) + (slotWidth / 2f)
                val left = centerX - (barWidth / 2f)
                val radius = barWidth / 2f

                // Capsule background track
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(left, topY),
                    size = Size(barWidth, availableHeight),
                    cornerRadius = CornerRadius(radius, radius)
                )

                // Active foreground capsule bar
                val ratio = (p.totalDurationMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
                val barHeight = (availableHeight * ratio * progress).coerceAtLeast(if (p.totalDurationMs > 0L) radius * 2 else 0f)
                val barTop = (topY + availableHeight) - barHeight

                val isToday = i == points.size - 1
                val isSelected = i == targetIndex

                val barColor = when {
                    isSelected -> CapsuleGoldBar
                    isToday -> CapsuleGoldBar
                    i % 2 == 0 -> CapsulePeachBar
                    else -> CapsuleGoldBar
                }

                if (barHeight > 0f) {
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(left, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(radius, radius)
                    )
                }

                val nodeCenter = Offset(centerX, barTop)
                nodePoints.add(nodeCenter)
            }

            // Update callout anchor for target day
            if (targetIndex in nodePoints.indices) {
                calloutAnchor = nodePoints[targetIndex]
            }

            // 2. Connected o—o dark ink trend line overlay
            if (nodePoints.size > 1) {
                for (i in 0 until nodePoints.size - 1) {
                    drawLine(
                        color = trendLineColor,
                        start = nodePoints[i],
                        end = nodePoints[i + 1],
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                nodePoints.forEachIndexed { i, pt ->
                    val isTarget = i == targetIndex
                    if (isTarget) {
                        drawCircle(
                            color = trendLineColor,
                            radius = 5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = if (isPastelCard) SageMintInnerCallout else CharcoalBentoSurface,
                            radius = 3.dp.toPx(),
                            center = pt
                        )
                    } else {
                        drawCircle(
                            color = trendLineColor,
                            radius = 3.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = cardBackgroundColor,
                            radius = 1.8.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }
        }

        // Floating interactive tap-to-inspect callout pill
        if (targetPoint != null && calloutAnchor != null) {
            val anchor = calloutAnchor!!
            val density = LocalDensity.current
            val pillHalfWidthPx = with(density) { 43.dp.toPx() }
            val badgeLiftPx = with(density) { 28.dp.toPx() }
            val minTopPx = with(density) { 4.dp.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPastelCard) SageMintInnerCallout else ButterGoldAccent,
                    shadowElevation = 2.dp,
                    modifier = Modifier.offset {
                        val clampedX = (anchor.x - pillHalfWidthPx).coerceAtLeast(0f)
                        val clampedY = (anchor.y - badgeLiftPx).coerceAtLeast(minTopPx)
                        IntOffset(clampedX.roundToInt(), clampedY.roundToInt())
                    }
                ) {
                    Text(
                        text = "${targetPoint.dayLabel}: ${targetPoint.formattedDuration}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PastelCardInk,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Bottom day labels row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp)
                .height(24.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            points.forEachIndexed { index, point ->
                val isTarget = index == targetIndex
                Text(
                    text = point.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Medium,
                    color = when {
                        isTarget && isPastelCard -> PastelCardInk
                        isTarget -> ButterGoldAccent
                        isPastelCard -> PastelCardMutedInk
                        else -> MutedChampagneIcon
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Updated 7-Day Usage Bar Chart rendering interactive capsule bars and trend line overlay.
 * Compatible with existing callers across dark bento cards and detail screens.
 */
@Composable
fun SevenDayUsageBarChart(
    points: List<DailyUsagePoint>,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 190.dp
) {
    SoftOliveCapsuleBarChart(
        points = points,
        modifier = modifier,
        cardBackgroundColor = CharcoalBentoSurface,
        isPastelCard = false,
        chartHeight = chartHeight
    )
}
