package com.whatspent.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.whatspent.app.ui.theme.AcidLimeAccent
import com.whatspent.app.ui.theme.ButterGoldAccent
import com.whatspent.app.ui.theme.CharcoalBentoSurface
import com.whatspent.app.ui.theme.ElevatedCharcoalSurface
import com.whatspent.app.ui.theme.FloatingTooltipBorder
import com.whatspent.app.ui.theme.FloatingTooltipSurface
import com.whatspent.app.ui.theme.InactiveBarCharcoal
import com.whatspent.app.ui.theme.MutedFractionText
import com.whatspent.app.ui.theme.ObsidianBackground
import com.whatspent.app.ui.theme.OnAcidLimeInk
import com.whatspent.app.ui.theme.PeriwinkleBarHighlight
import com.whatspent.app.ui.theme.PrimaryWhiteText
import com.whatspent.app.ui.theme.SubtleAxisLabelText
import com.whatspent.app.ui.theme.SubtleDarkBorder
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
 * Animated 180° Semi-Circular Arch Gauge Chart matching the Obsidian + Acid-Lime aesthetic.
 *
 * - Draws a thick 28.dp arch from 180° to 360° across the top half with an InactiveBarCharcoal track.
 * - Proportional multi-colored segments from ChatChartPalette separated by crisp 2° gaps.
 * - Tap gesture detection on segments to select/deselect a chat.
 * - Floating dark glass pill badge in the center showing duration (18.sp bold white)
 *   and chat name (12.sp muted), with interactive neon acid-lime pager dots underneath.
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
    trackColor: Color = InactiveBarCharcoal,
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

            // Floating dark glass pill badge in the lower arch center
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FloatingTooltipSurface,
                    border = BorderStroke(1.dp, FloatingTooltipBorder),
                    shadowElevation = 3.dp,
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
                            color = PrimaryWhiteText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = displayChat?.displayName ?: "No chats today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SubtleAxisLabelText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Interactive pager dots underneath the callout pill
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
                                        if (isActive) AcidLimeAccent else SubtleDarkBorder
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
                            segment.color.copy(alpha = 0.20f)
                        } else {
                            ElevatedCharcoalSurface
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) segment.color.copy(alpha = 0.5f) else SubtleDarkBorder
                        ),
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
                                color = PrimaryWhiteText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${segment.summary.percentage.roundToInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = SubtleAxisLabelText
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
 * 7-Day Trend Chart styled with sleek dark charcoal surface, Acid-Lime cubic curve,
 * subtle horizontal dashed gridlines, and floating dark glass callout badge.
 */
@Composable
fun WarmSandBezierTrendChart(
    points: List<DailyUsagePoint>,
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = CharcoalBentoSurface,
    calloutPillColor: Color = FloatingTooltipSurface,
    lineColor: Color = AcidLimeAccent,
    gridColor: Color = SubtleDarkBorder,
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
                color = SubtleAxisLabelText
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
                color = SubtleAxisLabelText
            )
            Text(
                text = formatDuration(maxDurationMs / 2),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = SubtleAxisLabelText
            )
            Text(
                text = "0m",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = SubtleAxisLabelText
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
                            lineColor.copy(alpha = 0.22f),
                            lineColor.copy(alpha = 0.01f)
                        ),
                        startY = topY,
                        endY = topY + usableHeight
                    )
                )

                // 5. Stroke the smooth cubic Bezier line
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
                            color = ObsidianBackground,
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

        // Floating dark glass callout badge
        if (targetPoint != null && badgeOffset != null) {
            val offset = badgeOffset!!
            val density = LocalDensity.current
            val pillHalfWidthPx = with(density) { 48.dp.toPx() }
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
                    border = BorderStroke(1.dp, FloatingTooltipBorder),
                    shadowElevation = 3.dp,
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
                        color = PrimaryWhiteText,
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
                    color = if (isTarget) PrimaryWhiteText else SubtleAxisLabelText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// 3. SOLANA-STYLE DARK BAR CHART (Pitch Black + Acid-Lime / Periwinkle Violet)
// ============================================================================

/**
 * Solana-style dark mode Bar Chart matching the reference design.
 *
 * - Pitch-black canvas with horizontal dashed reference lines at 100%, 50%, and 0%.
 * - Crisp right-aligned Y-axis labels (e.g. 2h, 1h, 0m).
 * - Thick rounded vertical bars (CornerRadius 14.dp):
 *   - Unselected bars: Deep matte charcoal (#222227).
 *   - Selected / Active / Peak bar: Vibrant Periwinkle Violet (#9F8FEF) with subtle top glow/gradient.
 * - Floating dark glass tooltip pill (#232329, 14.dp corners, #34343C border) hovering directly
 *   above the selected bar with two-tone primary + muted duration text.
 * - Clean X-axis day labels with interactive tap selection.
 */
@Composable
fun SolanaStyleBarChart(
    points: List<DailyUsagePoint>,
    modifier: Modifier = Modifier,
    highlightColor: Color = PeriwinkleBarHighlight,
    inactiveBarColor: Color = InactiveBarCharcoal,
    gridLineColor: Color = SubtleDarkBorder,
    axisLabelColor: Color = SubtleAxisLabelText,
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
                color = axisLabelColor
            )
        }
        return
    }

    var internalSelectedDay by remember { mutableStateOf<Int?>(null) }
    val effectiveSelectedDay = selectedDayIndex ?: internalSelectedDay

    val datesKey = remember(points) { points.map { it.date } }
    val animationProgress = remember { Animatable(1f) }
    LaunchedEffect(datesKey) {
        animationProgress.snapTo(0.2f)
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

    val leftPadding = 8.dp
    val rightPadding = 42.dp
    val topPadding = 38.dp
    val bottomPadding = 26.dp
    val usableHeightDp = chartHeight - topPadding - bottomPadding

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight)
    ) {
        var tooltipAnchor by remember { mutableStateOf<Offset?>(null) }

        // 1. Right-side Y-Axis Labels (Top, Middle, Bottom)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 4.dp)
        ) {
            Text(
                text = formatDuration(maxDurationMs),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = axisLabelColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = topPadding - 8.dp)
            )
            Text(
                text = formatDuration(maxDurationMs / 2),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = axisLabelColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = topPadding + (usableHeightDp * 0.5f) - 8.dp)
            )
            Text(
                text = "0m",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = axisLabelColor,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = topPadding + usableHeightDp - 8.dp)
            )
        }

        // 2. Canvas for Dashed Reference Lines & Vertical Rounded Bars
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = leftPadding, end = rightPadding)
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val count = points.size
                        val slotWidth = size.width / count
                        val tappedIdx = (tapOffset.x / slotWidth).toInt().coerceIn(0, points.size - 1)
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

            // Dashed horizontal gridlines (100%, 50%, 0%)
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            listOf(0f, 0.5f, 1f).forEach { ratio ->
                val gridY = topY + (usableHeight * ratio)
                drawLine(
                    color = gridLineColor,
                    start = Offset(0f, gridY),
                    end = Offset(usableWidth, gridY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashEffect
                )
            }

            // Draw thick rounded vertical bars
            val count = points.size
            val slotWidth = usableWidth / count
            val barWidth = (slotWidth * 0.46f).coerceIn(14.dp.toPx(), 26.dp.toPx())
            val cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())

            points.forEachIndexed { i, p ->
                val centerX = (i * slotWidth) + (slotWidth / 2f)
                val left = centerX - (barWidth / 2f)
                val ratio = (p.totalDurationMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
                val animatedHeight = (usableHeight * ratio * progress)
                    .coerceAtLeast(if (p.totalDurationMs > 0L) 12.dp.toPx() else 6.dp.toPx())
                val barTop = (topY + usableHeight) - animatedHeight
                val isSelected = i == targetIndex

                if (isSelected) {
                    // Vibrant Periwinkle Violet with subtle top glow
                    val glowBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFB5A7FF),
                            highlightColor
                        ),
                        startY = barTop,
                        endY = barTop + animatedHeight
                    )
                    drawRoundRect(
                        brush = glowBrush,
                        topLeft = Offset(left, barTop),
                        size = Size(barWidth, animatedHeight),
                        cornerRadius = cornerRadius
                    )
                    tooltipAnchor = Offset(centerX, barTop)
                } else {
                    // Deep matte charcoal inactive bar
                    drawRoundRect(
                        color = inactiveBarColor,
                        topLeft = Offset(left, barTop),
                        size = Size(barWidth, animatedHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }

        // 3. Floating Dark Glass Tooltip Pill Hovering Directly Above Selected Bar
        if (targetPoint != null && tooltipAnchor != null) {
            val anchor = tooltipAnchor!!
            val density = LocalDensity.current
            val pillHalfWidthPx = with(density) { 48.dp.toPx() }
            val pillLiftPx = with(density) { 34.dp.toPx() }
            val minTopPx = with(density) { 2.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = leftPadding, end = rightPadding)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = FloatingTooltipSurface,
                    border = BorderStroke(1.dp, FloatingTooltipBorder),
                    shadowElevation = 4.dp,
                    modifier = Modifier.offset {
                        val clampedX = (anchor.x - pillHalfWidthPx).coerceAtLeast(0f)
                        val clampedY = (anchor.y - pillLiftPx).coerceAtLeast(minTopPx)
                        IntOffset(clampedX.roundToInt(), clampedY.roundToInt())
                    }
                ) {
                    val durationParts = targetPoint.formattedDuration.split(" ")
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (durationParts.size >= 2) {
                            Text(
                                text = durationParts[0],
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryWhiteText
                            )
                            Text(
                                text = durationParts[1],
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MutedFractionText
                            )
                        } else {
                            Text(
                                text = targetPoint.formattedDuration,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryWhiteText
                            )
                        }
                        Text(
                            text = "• ${targetPoint.dayLabel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = axisLabelColor
                        )
                    }
                }
            }
        }

        // 4. X-Axis Day Labels Row at Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = leftPadding, end = rightPadding)
                .height(bottomPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            points.forEachIndexed { index, point ->
                val isSelected = index == targetIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (onDaySelected != null) {
                                onDaySelected(if (targetIndex == index) null else index)
                            } else {
                                internalSelectedDay = if (internalSelectedDay == index) null else index
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = point.dayLabel,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryWhiteText else axisLabelColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ============================================================================
// 4. CAPSULE & SEVEN DAY BAR CHART COMPATIBILITY BRIDGES
// ============================================================================

/**
 * Capsule Bar Chart now powered by the sleek [SolanaStyleBarChart] design.
 * Fully compatible with existing call sites.
 */
@Composable
fun SoftOliveCapsuleBarChart(
    points: List<DailyUsagePoint>,
    modifier: Modifier = Modifier,
    cardBackgroundColor: Color = CharcoalBentoSurface,
    isPastelCard: Boolean = false,
    chartHeight: Dp = 190.dp,
    selectedDayIndex: Int? = null,
    onDaySelected: ((Int?) -> Unit)? = null
) {
    SolanaStyleBarChart(
        points = points,
        modifier = modifier,
        chartHeight = chartHeight,
        selectedDayIndex = selectedDayIndex,
        onDaySelected = onDaySelected
    )
}

/**
 * 7-Day Usage Bar Chart rendering interactive rounded bars, Periwinkle Violet highlight,
 * and floating dark glass tooltip pill.
 */
@Composable
fun SevenDayUsageBarChart(
    points: List<DailyUsagePoint>,
    highlightColor: Color = PeriwinkleBarHighlight,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 190.dp,
    selectedDayIndex: Int? = null,
    onDaySelected: ((Int?) -> Unit)? = null
) {
    SolanaStyleBarChart(
        points = points,
        highlightColor = highlightColor,
        modifier = modifier,
        chartHeight = chartHeight,
        selectedDayIndex = selectedDayIndex,
        onDaySelected = onDaySelected
    )
}
