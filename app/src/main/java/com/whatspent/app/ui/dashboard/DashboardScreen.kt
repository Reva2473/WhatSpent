package com.whatspent.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.whatspent.app.data.model.ActiveSessionState
import com.whatspent.app.data.model.ChatSummary
import com.whatspent.app.data.model.DailyUsagePoint
import com.whatspent.app.data.model.DashboardUiState
import com.whatspent.app.data.model.formatDurationWithSeconds
import com.whatspent.app.util.PermissionHelper
import com.whatspent.app.util.TimeFormatUtils
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

// ============================================================================
// WHATSPENT DESIGN SYSTEM TOKENS (Obsidian + Neon Acid-Lime + Periwinkle)
// Direct Color(0xFF...) constants to guarantee zero cross-worker compile conflicts
// ============================================================================

private val BgObsidian = Color(0xFF0B0B0D)          // True pitch-black app canvas
private val SurfaceCharcoal = Color(0xFF17171A)     // Deep dark charcoal card surfaces
private val ElevatedCharcoal = Color(0xFF1E1E22)    // Elevated pill tracks & borders
private val CircleButtonBg = Color(0xFF1C1C20)      // Circular icon button surface
private val BarInactiveCharcoal = Color(0xFF222227) // Inactive/unselected chart bars
private val TooltipDarkSurface = Color(0xFF232329)  // Floating dark tooltip pill
private val TooltipBorder = Color(0xFF34343C)       // Tooltip border
private val BorderSubtle = Color(0xFF28282D)        // Subtle 1.dp borders & dashed gridlines

private val AcidLime = Color(0xFFE5FA6B)            // Neon Acid-Lime primary active highlight
private val AcidLimeInk = Color(0xFF0E0F0A)         // Dark ink text on AcidLime
private val PeriwinkleViolet = Color(0xFF9F8FEF)    // Signature Periwinkle Violet chart highlight
private val PeriwinkleLight = Color(0xFFB4A6F8)     // Top glow tint on Periwinkle bar

private val TextWhite = Color(0xFFF6F6F9)           // Crisp white primary text
private val TextMutedGray = Color(0xFF6E6E77)        // Two-tone secondary time units (e.g. 14m, 32s)
private val TextSubtleLabel = Color(0xFF8A8A93)      // Subtle gray for subtitles, counts, & axes

// Additional harmonious dark accents for distribution & avatars
private val CyberMint = Color(0xFF63E6BE)
private val ElectricSky = Color(0xFF74C0FC)
private val SoftCoral = Color(0xFFFF8787)
private val WarmAmber = Color(0xFFFCC419)
private val OrchidViolet = Color(0xFFE599F7)

private val ChatAccentPalette = listOf(
    PeriwinkleViolet,
    AcidLime,
    CyberMint,
    ElectricSky,
    SoftCoral,
    WarmAmber,
    OrchidViolet
)

/**
 * Deterministically maps a chat to an accent color from the dark palette.
 */
private fun getChatAccentColor(chatName: String, customColorTag: Int? = null): Color {
    if (customColorTag != null) {
        return Color(customColorTag)
    }
    val hash = abs(chatName.hashCode())
    return ChatAccentPalette[hash % ChatAccentPalette.size]
}

/**
 * Helper holder for Two-Tone duration typography (matching "24.446 00" in the reference image).
 */
private data class TwoToneDuration(val primary: String, val secondary: String)

/**
 * Splits duration into primary and secondary parts for high-impact two-tone typography.
 */
private fun splitDurationTwoTone(durationMs: Long): TwoToneDuration {
    if (durationMs <= 0L) {
        return TwoToneDuration("0m", " 00s")
    }
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> {
            TwoToneDuration("${hours}h", " ${minutes}m")
        }
        minutes > 0 -> {
            TwoToneDuration("${minutes}m", " ${String.format(Locale.US, "%02d", seconds)}s")
        }
        else -> {
            TwoToneDuration("$seconds", "s")
        }
    }
}

/**
 * Splits duration into primary and secondary parts for compact conversation rows.
 */
private fun splitDurationTwoToneShort(durationMs: Long): TwoToneDuration {
    if (durationMs <= 0L) {
        return TwoToneDuration("0m", "")
    }
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> {
            if (minutes > 0) TwoToneDuration("${hours}h", " ${minutes}m") else TwoToneDuration("${hours}h", "")
        }
        minutes > 0 -> {
            TwoToneDuration("${minutes}m", if (seconds > 0) " ${seconds}s" else "")
        }
        else -> {
            TwoToneDuration("${seconds}s", "")
        }
    }
}

// ============================================================================
// MAIN DASHBOARD SCREEN (Zero Nested Scaffold, Zero Top/Bottom Dead Space)
// ============================================================================

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onNavigateToChatDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onSeedDemoData: () -> Unit,
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

    // Top pill categories: 0: Overview, 1: Activity, 2: Chats, 3: + Sample
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Chart mode: 0: 7d Trend, 1: Top Chats, 2: Sessions, 3: Stats
    var selectedChartMode by remember { mutableIntStateOf(0) }

    val filteredChats = remember(uiState.chatSummaries, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.chatSummaries
        } else {
            uiState.chatSummaries.filter {
                it.displayName.contains(searchQuery, ignoreCase = true) ||
                        it.chatName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val topChats = remember(uiState.chatSummaries) {
        uiState.chatSummaries.filter { !it.isExcluded && it.durationMs > 0L }.take(5)
    }

    // Handle loading state
    if (uiState.isLoading) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BgObsidian),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AcidLime, strokeWidth = 3.dp)
        }
        return
    }

    // Main scrollable content flush against navigation padding (NO nested Scaffold!)
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BgObsidian),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Compact Top Header Row
        item(key = "top_header") {
            DashboardTopHeader(
                isCoreTrackingReady = permissionStatus.isCoreTrackingReady,
                isTracking = uiState.activeSession.isTracking,
                isSearchOpen = isSearchOpen,
                onToggleSearch = { isSearchOpen = !isSearchOpen },
                onOpenSettings = onNavigateToSettings,
                onOpenOnboarding = onNavigateToOnboarding
            )
        }

        // 1.1 Inline Search Bar (Animated visibility)
        item(key = "inline_search") {
            AnimatedVisibility(
                visible = isSearchOpen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                InlineSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        searchQuery = ""
                        isSearchOpen = false
                    }
                )
            }
        }

        // 2. Top Category Pill Strip (Overview, Activity, Chats, + Sample)
        item(key = "category_pill_strip") {
            TopCategoryPillStrip(
                selectedIndex = selectedCategoryIndex,
                onSelectCategory = { index ->
                    if (index == 3) {
                        onSeedDemoData()
                    } else {
                        selectedCategoryIndex = index
                    }
                }
            )
        }

        // 3. Live Active WhatsApp Session Banner (when tracking)
        if (uiState.activeSession.isTracking) {
            item(key = "live_session_banner") {
                LiveActiveWhatsAppBanner(
                    activeSession = uiState.activeSession,
                    onClick = {
                        uiState.activeSession.currentChatName?.let { onNavigateToChatDetail(it) }
                    }
                )
            }
        }

        // 4. Hero Screen Time Header + Two-Tone Typography + Pie Toggle Button
        if (selectedCategoryIndex == 0 || selectedCategoryIndex == 1 || selectedCategoryIndex == 2) {
            item(key = "hero_time_header") {
                HeroScreenTimeHeader(
                    todayTotalMs = uiState.todayTotalMs,
                    comparisonText = uiState.comparisonText,
                    isPieMode = selectedChartMode == 1,
                    onToggleChartMode = {
                        selectedChartMode = if (selectedChartMode == 1) 0 else 1
                    }
                )
            }
        }

        // 5. Interactive Hero Chart Section (Bar Chart Mode or Top Chats Mode + Segmented Switcher)
        if (selectedCategoryIndex == 0 || selectedCategoryIndex == 1) {
            item(key = "hero_chart_section") {
                InteractiveHeroChartSection(
                    points = uiState.sevenDayUsage,
                    todayTotalMs = uiState.todayTotalMs,
                    topChats = topChats,
                    selectedChartMode = selectedChartMode,
                    onChartModeChange = { selectedChartMode = it },
                    onChatClick = onNavigateToChatDetail
                )
            }
        }

        // 6. Quick Summary Strip (Active Chats, Sessions, Peak Chat)
        if (selectedCategoryIndex == 0 || selectedCategoryIndex == 1) {
            item(key = "quick_summary_strip") {
                val topChat = topChats.firstOrNull()
                QuickSummaryStrip(
                    activeChatsCount = uiState.chatSummaries.count { !it.isExcluded && it.durationMs > 0L },
                    totalSessionsCount = uiState.chatSummaries.sumOf { it.sessionCount },
                    topChatDisplayName = topChat?.displayName ?: "None",
                    onTopChatClick = {
                        topChat?.let { onNavigateToChatDetail(it.chatName) }
                    }
                )
            }
        }

        // 7. Ranked Conversation List Header & Items
        if (selectedCategoryIndex == 0 || selectedCategoryIndex == 2) {
            item(key = "ranked_conversations_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Conversations",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${filteredChats.size} today",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSubtleLabel
                    )
                }
            }

            if (filteredChats.isEmpty()) {
                item(key = "empty_conversations") {
                    EmptyConversationsCard(onSeedDemoData = onSeedDemoData)
                }
            } else {
                itemsIndexed(
                    items = filteredChats,
                    key = { _, chat -> "chat_${chat.chatName}" }
                ) { index, chat ->
                    ConversationRowItem(
                        chat = chat,
                        index = index,
                        onClick = { onNavigateToChatDetail(chat.chatName) }
                    )
                }
            }
        }

        // Bottom spacer kept minimal (at most 4.dp) to eliminate bottom dead space
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// ============================================================================
// 1. TOP HEADER COMPONENT (Flush at top, search + title + status + settings)
// ============================================================================

@Composable
private fun DashboardTopHeader(
    isCoreTrackingReady: Boolean,
    isTracking: Boolean,
    isSearchOpen: Boolean,
    onToggleSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 40dp Circular dark button with Search icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CircleButtonBg)
                .clickable(onClick = onToggleSearch),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search chats",
                modifier = Modifier.size(19.dp),
                tint = if (isSearchOpen) AcidLime else TextWhite
            )
        }

        // Center: "WhatSpent" Title
        Text(
            text = "WhatSpent",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextWhite
        )

        // Right: Status indicator pill + 40dp Settings button with Tune icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isCoreTrackingReady) {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenSettings),
                    color = Color(0xFF131A14),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF233A26))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PulsingDot(
                            isPulsing = isTracking,
                            color = AcidLime,
                            size = 7.dp
                        )
                        Text(
                            text = if (isTracking) "Tracking" else "Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AcidLime
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenOnboarding),
                    color = Color(0xFF2B1813),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF4C2719))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = Color(0xFFFF922B)
                        )
                        Text(
                            text = "Setup",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF922B)
                        )
                    }
                }
            }

            // Rightmost: 40dp circular button with Tune (sliders) icon matching reference image
            Box(
                modifier = Modifier
                    .size(40.dp)
                .clip(CircleShape)
                .background(CircleButtonBg)
                .clickable(onClick = onOpenSettings),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings",
                    modifier = Modifier.size(19.dp),
                    tint = TextWhite
                )
            }
        }
    }
}

// ============================================================================
// 1.1 INLINE SEARCH BAR
// ============================================================================

@Composable
private fun InlineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(14.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = TextSubtleLabel
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search conversations...",
                        style = TextStyle(
                            color = TextSubtleLabel,
                            fontSize = 14.sp
                        )
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(AcidLime),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear search",
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                    tint = TextSubtleLabel
                )
            }
        }
    }
}

// ============================================================================
// 2. TOP CATEGORY PILL STRIP (Overview, Activity, Chats, + Sample)
// ============================================================================

@Composable
private fun TopCategoryPillStrip(
    selectedIndex: Int,
    onSelectCategory: (Int) -> Unit
) {
    val categories = remember {
        listOf("Overview", "Activity", "Chats", "+ Sample")
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(categories) { index, category ->
            val isSelected = selectedIndex == index
            val isActionPill = index == 3

            val pillBg by animateColorAsState(
                targetValue = when {
                    isSelected -> AcidLime
                    isActionPill -> CircleButtonBg
                    else -> SurfaceCharcoal
                },
                label = "pillBg_$index"
            )

            val textColor by animateColorAsState(
                targetValue = when {
                    isSelected -> AcidLimeInk
                    isActionPill -> AcidLime
                    else -> TextSubtleLabel
                },
                label = "pillTextColor_$index"
            )

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { onSelectCategory(index) },
                shape = RoundedCornerShape(50),
                color = pillBg,
                border = if (!isSelected) BorderStroke(1.dp, BorderSubtle) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isActionPill) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = AcidLime
                        )
                    }
                    Text(
                        text = if (isActionPill) "Sample" else category,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected || isActionPill) FontWeight.SemiBold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// 3. LIVE ACTIVE WHATSAPP SESSION BANNER
// ============================================================================

@Composable
private fun LiveActiveWhatsAppBanner(
    activeSession: ActiveSessionState,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, Color(0xFF353820))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                PulsingDot(
                    isPulsing = true,
                    color = AcidLime,
                    size = 10.dp
                )
                Column {
                    Text(
                        text = "NOW ACTIVE IN WHATSAPP",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AcidLime,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeSession.currentDisplayName ?: "Conversation",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDurationWithSeconds(activeSession.currentSessionDurationMs),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AcidLime
                )
                Text(
                    text = "Today: ${TimeFormatUtils.formatDurationShort(activeSession.currentChatTodayTotalMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextSubtleLabel
                )
            }
        }
    }
}

// ============================================================================
// 4. HERO SCREEN TIME HEADER + TWO-TONE HERO NUMBER + ACID-LIME PIE BUTTON
// (Directly modeled on SOL v + 24.446 00 + Pie button in reference image)
// ============================================================================

@Composable
private fun HeroScreenTimeHeader(
    todayTotalMs: Long,
    comparisonText: String,
    isPieMode: Boolean,
    onToggleChartMode: () -> Unit
) {
    val twoTone = remember(todayTotalMs) {
        splitDurationTwoTone(todayTotalMs)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Selector & Action Row: "Today's Screen Time v" + "+15% vs yesterday" + 40dp Pie button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Selector pill matching the SOL v dropdown in the reference image
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCharcoal)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(AcidLime)
                    )
                    Text(
                        text = "Today's Screen Time",
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextSubtleLabel
                    )
                }

                // Comparison badge (+15% vs yesterday)
                if (comparisonText.isNotBlank() && comparisonText != "No data yesterday") {
                    Text(
                        text = comparisonText,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AcidLime
                    )
                }
            }

            // Right: 40dp Circular Dark Button with Neon Acid-Lime Pie Chart Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CircleButtonBg)
                    .border(1.dp, BorderSubtle, CircleShape)
                    .clickable(onClick = onToggleChartMode),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPieMode) Icons.Default.BarChart else Icons.Default.PieChart,
                    contentDescription = "Toggle Chart Mode",
                    modifier = Modifier.size(20.dp),
                    tint = AcidLime
                )
            }
        }

        // Giant Two-Tone Typography Display (matching 24.446 00)
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.padding(top = 2.dp)
        ) {
            Text(
                text = twoTone.primary,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = twoTone.secondary,
                style = MaterialTheme.typography.displayMedium,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextMutedGray,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

// ============================================================================
// 5. INTERACTIVE HERO CHART SECTION
// (Mode A: 7-Day Bar Chart with Floating Tooltip & Glowing Periwinkle Bar)
// (Mode B: Top Chats Distribution Gauge)
// (Bottom: 1d 1w 1m [6m] 1y Segmented Pill Switcher)
// ============================================================================

@Composable
private fun InteractiveHeroChartSection(
    points: List<DailyUsagePoint>,
    todayTotalMs: Long,
    topChats: List<ChatSummary>,
    selectedChartMode: Int,
    onChartModeChange: (Int) -> Unit,
    onChatClick: (String) -> Unit
) {
    val displayPoints = remember(points) {
        if (points.isNotEmpty()) {
            points.takeLast(7)
        } else {
            (0..6).map { i ->
                DailyUsagePoint(
                    date = TimeFormatUtils.dateStringDaysAgo(6 - i),
                    dayLabel = when (i) {
                        0 -> "Mon"; 1 -> "Tue"; 2 -> "Wed"; 3 -> "Thu"; 4 -> "Fri"; 5 -> "Sat"; else -> "Sun"
                    },
                    totalDurationMs = 0L
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (selectedChartMode) {
            0 -> {
                // Mode A: Interactive 7-Day Activity Bar Chart
                ActivityBarChart7Day(
                    points = displayPoints,
                    todayTotalMs = todayTotalMs
                )
            }
            1 -> {
                // Mode B: Top Chats Distribution Chart
                TopChatsDistributionChart(
                    topChats = topChats,
                    onChatClick = onChatClick
                )
            }
            2 -> {
                // Mode C: Sessions Breakdown
                SessionsBreakdownCard(
                    topChats = topChats,
                    onTopChatClick = onChatClick
                )
            }
            else -> {
                // Mode D: Stats Overview
                QuickStatsOverviewCard(
                    todayTotalMs = todayTotalMs,
                    points = displayPoints
                )
            }
        }

        // Segmented Pill Switcher below chart (matching 1d 1w 1m [6m] 1y in reference image)
        SegmentedChartSwitcher(
            selectedMode = selectedChartMode,
            onModeSelected = onChartModeChange
        )
    }
}

/**
 * 7-Day Activity Bar Chart:
 * 3 dashed horizontal reference lines with right-aligned duration labels.
 * 7 vertical thick rounded bars (inactive matte charcoal, active periwinkle violet with top glow).
 * Floating dark tooltip pill over selected bar in two-tone typography.
 */
@Composable
private fun ActivityBarChart7Day(
    points: List<DailyUsagePoint>,
    todayTotalMs: Long
) {
    // Default selection to the last day (Today) or peak day
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val activeIndex = (selectedIndex ?: (points.size - 1)).coerceIn(0, (points.size - 1).coerceAtLeast(0))
    val selectedPoint = points.getOrNull(activeIndex)

    val maxDuration = remember(points, todayTotalMs) {
        val maxVal = points.maxOfOrNull { it.totalDurationMs } ?: todayTotalMs
        if (maxVal <= 0L) 3600_000L else maxVal
    }

    val maxLabel = remember(maxDuration) { formatChartAxisLabel(maxDuration) }
    val midLabel = remember(maxDuration) { formatChartAxisLabel(maxDuration / 2) }

    val selectedTwoTone = remember(selectedPoint) {
        splitDurationTwoTone(selectedPoint?.totalDurationMs ?: 0L)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        val density = LocalDensity.current
        val totalWidthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()

        val rightAxisWidth = 34.dp
        val rightAxisWidthPx = with(density) { rightAxisWidth.toPx() }
        val chartAreaWidthPx = totalWidthPx - rightAxisWidthPx
        val slotWidthPx = chartAreaWidthPx / points.size.coerceAtLeast(1)

        val tooltipWidth = 118.dp
        val tooltipWidthPx = with(density) { tooltipWidth.toPx() }

        // Compute active bar center and tooltip X position
        val activeBarCenterX = (activeIndex + 0.5f) * slotWidthPx
        val targetTooltipXPx = (activeBarCenterX - tooltipWidthPx / 2f).coerceIn(4f, totalWidthPx - tooltipWidthPx - 4f)
        val targetTooltipXDp = with(density) { targetTooltipXPx.toDp() }

        val animatedTooltipX by animateDpAsState(
            targetValue = targetTooltipXDp,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "tooltipSlideX"
        )

        // 1. Right Axis Labels (Max, Mid, 0m)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(rightAxisWidth)
                .padding(top = 28.dp, bottom = 26.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = maxLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = TextSubtleLabel,
                textAlign = TextAlign.End
            )
            Text(
                text = midLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = TextSubtleLabel,
                textAlign = TextAlign.End
            )
            Text(
                text = "0m",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = TextSubtleLabel,
                textAlign = TextAlign.End
            )
        }

        // 2. Interactive Canvas: Dashed Gridlines, Glowing Bars & Inactive Bars
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val tapped = (tapOffset.x / slotWidthPx).toInt()
                        if (tapped in points.indices) {
                            selectedIndex = tapped
                        }
                    }
                }
        ) {
            val usableHeight = size.height - 66.dp.toPx()
            val chartTopY = 36.dp.toPx()
            val baselineY = chartTopY + usableHeight
            val midY = chartTopY + (usableHeight / 2f)

            // Draw 3 horizontal dashed reference lines
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            val gridColor = BorderSubtle

            // Top dashed line
            drawLine(
                color = gridColor,
                start = Offset(0f, chartTopY),
                end = Offset(chartAreaWidthPx, chartTopY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )

            // Middle dashed line
            drawLine(
                color = gridColor,
                start = Offset(0f, midY),
                end = Offset(chartAreaWidthPx, midY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )

            // Bottom baseline dashed line
            drawLine(
                color = gridColor,
                start = Offset(0f, baselineY),
                end = Offset(chartAreaWidthPx, baselineY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect
            )

            // Draw 7 thick vertical rounded bars
            val barWidthPx = (slotWidthPx * 0.52f).coerceIn(20.dp.toPx(), 30.dp.toPx())

            points.forEachIndexed { i, point ->
                val centerX = (i * slotWidthPx) + (slotWidthPx / 2f)
                val left = centerX - (barWidthPx / 2f)
                val ratio = (point.totalDurationMs.toFloat() / maxDuration.toFloat()).coerceIn(0.06f, 1f)
                val barH = usableHeight * ratio
                val top = baselineY - barH
                val isSelected = i == activeIndex

                if (isSelected) {
                    // Subtle glowing beam rising from top of periwinkle bar to tooltip
                    val beamTop = 32.dp.toPx()
                    if (top > beamTop) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    PeriwinkleViolet.copy(alpha = 0.22f)
                                )
                            ),
                            topLeft = Offset(left - 2.dp.toPx(), beamTop),
                            size = Size(barWidthPx + 4.dp.toPx(), top - beamTop),
                            cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                        )
                    }

                    // Selected Bar: Vibrant Periwinkle Violet with vertical gradient highlight
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                PeriwinkleLight,
                                PeriwinkleViolet
                            )
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidthPx, barH),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                } else {
                    // Inactive Bar: Deep matte charcoal
                    drawRoundRect(
                        color = BarInactiveCharcoal,
                        topLeft = Offset(left, top),
                        size = Size(barWidthPx, barH),
                        cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
                    )
                }
            }
        }

        // 3. Floating Dark Tooltip Pill (matching 27.354 00 SOL pill in reference image)
        if (selectedPoint != null) {
            Surface(
                modifier = Modifier
                    .offset(x = animatedTooltipX, y = 2.dp)
                    .width(tooltipWidth),
                shape = RoundedCornerShape(14.dp),
                color = TooltipDarkSurface,
                border = BorderStroke(1.dp, TooltipBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${selectedPoint.dayLabel} • ",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSubtleLabel
                    )
                    Text(
                        text = selectedTwoTone.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = selectedTwoTone.secondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMutedGray
                    )
                }
            }
        }

        // 4. Day Labels Below Bars (Mon, Tue, Wed...)
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(with(density) { chartAreaWidthPx.toDp() }),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            points.forEachIndexed { i, point ->
                val isSelected = i == activeIndex
                Text(
                    text = point.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) TextWhite else TextSubtleLabel,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { selectedIndex = i }
                )
            }
        }
    }
}

/**
 * Mode B: Top Chats Distribution Chart (180° Arch Gauge with Curated Palette)
 */
@Composable
private fun TopChatsDistributionChart(
    topChats: List<ChatSummary>,
    onChatClick: (String) -> Unit
) {
    val totalMs = remember(topChats) {
        topChats.sumOf { it.durationMs }.coerceAtLeast(1L)
    }

    var selectedChatIndex by remember { mutableIntStateOf(0) }
    val safeSelectedIndex = if (topChats.isNotEmpty()) selectedChatIndex.coerceIn(0, topChats.size - 1) else 0
    val activeChat = topChats.getOrNull(safeSelectedIndex)

    val activeChatTwoTone = remember(activeChat) {
        splitDurationTwoTone(activeChat?.durationMs ?: 0L)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Distribution by Chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "Today's top conversations",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = TextSubtleLabel
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CircleButtonBg)
                        .clickable { activeChat?.let { onChatClick(it.chatName) } },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = "Chat Detail",
                        modifier = Modifier.size(16.dp),
                        tint = TextWhite
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 180° Semi-Circular Arch Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(topChats, totalMs) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height - 10.dp.toPx())
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val strokePx = 24.dp.toPx()
                                val outerRadius = (size.width / 2f) - (strokePx / 2f)
                                val innerRadius = outerRadius - strokePx

                                if (dist in (innerRadius * 0.7f)..(outerRadius * 1.3f) && dy <= 0f) {
                                    val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    val normalizedAngle = angle + 180f
                                    var currentStart = 0f
                                    val gap = if (topChats.size > 1) 2.5f else 0f
                                    val totalUsable = 180f - (gap * (topChats.size - 1))

                                    topChats.forEachIndexed { i, chat ->
                                        val ratio = chat.durationMs.toFloat() / totalMs.toFloat()
                                        val sweep = ratio * totalUsable
                                        if (normalizedAngle in currentStart..(currentStart + sweep + gap)) {
                                            selectedChatIndex = i
                                            return@detectTapGestures
                                        }
                                        currentStart += sweep + gap
                                    }
                                }
                            }
                        }
                ) {
                    val strokeWidthPx = 24.dp.toPx()
                    val center = Offset(size.width / 2f, size.height - 10.dp.toPx())
                    val radius = (size.width / 2f) - (strokeWidthPx / 2f) - 6.dp.toPx()
                    val arcTopLeft = Offset(center.x - radius, center.y - radius)
                    val arcSize = Size(radius * 2f, radius * 2f)

                    if (topChats.isEmpty()) {
                        drawArc(
                            color = BarInactiveCharcoal,
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    } else {
                        val gap = if (topChats.size > 1) 2.5f else 0f
                        val totalUsableSweep = 180f - (gap * (topChats.size - 1))
                        var currentAngle = 180f

                        topChats.forEachIndexed { index, chat ->
                            val color = ChatAccentPalette.getOrElse(index) { PeriwinkleViolet }
                            val ratio = (chat.durationMs.toFloat() / totalMs.toFloat()).coerceIn(0.05f, 1f)
                            val sweep = (ratio * totalUsableSweep).coerceAtLeast(3f)
                            val isSelected = index == safeSelectedIndex
                            val stroke = if (isSelected) strokeWidthPx + 5.dp.toPx() else strokeWidthPx

                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(width = stroke, cap = StrokeCap.Butt)
                            )
                            currentAngle += sweep + gap
                        }
                    }
                }

                // Center Callout Card inside Arch
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TooltipDarkSurface,
                    border = BorderStroke(1.dp, TooltipBorder),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clickable { activeChat?.let { onChatClick(it.chatName) } }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = activeChatTwoTone.primary,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextWhite
                            )
                            Text(
                                text = activeChatTwoTone.secondary,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextMutedGray
                            )
                        }
                        Text(
                            text = activeChat?.displayName ?: "No chats yet",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSubtleLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Segment selection indicator dots
            if (topChats.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    topChats.indices.forEach { index ->
                        val isCurrent = index == safeSelectedIndex
                        val dotColor = ChatAccentPalette.getOrElse(index) { PeriwinkleViolet }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isCurrent) 14.dp else 6.dp, 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) dotColor else BorderSubtle)
                                .clickable { selectedChatIndex = index }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mode C: Sessions Breakdown Card
 */
@Composable
private fun SessionsBreakdownCard(
    topChats: List<ChatSummary>,
    onTopChatClick: (String) -> Unit
) {
    val totalSessions = remember(topChats) { topChats.sumOf { it.sessionCount } }
    val topChat = topChats.firstOrNull()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Session Analytics",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Total Sessions Tile
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtleLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalSessions",
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcidLime
                        )
                        Text(
                            text = "sessions today",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextSubtleLabel
                        )
                    }
                }

                // Peak Conversation Tile
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { topChat?.let { onTopChatClick(it.chatName) } },
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "MOST ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtleLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = topChat?.displayName ?: "None",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PeriwinkleViolet,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${topChat?.sessionCount ?: 0} sessions",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextSubtleLabel
                        )
                    }
                }
            }
        }
    }
}

/**
 * Mode D: Quick Stats Overview Card
 */
@Composable
private fun QuickStatsOverviewCard(
    todayTotalMs: Long,
    points: List<DailyUsagePoint>
) {
    val weeklyAverageMs = remember(points) {
        if (points.isNotEmpty()) points.map { it.totalDurationMs }.average().toLong() else 0L
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Screen Time Overview",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "7-DAY AVERAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtleLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TimeFormatUtils.formatDurationShort(weeklyAverageMs),
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextWhite
                        )
                        Text(
                            text = "per day",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextSubtleLabel
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    color = ElevatedCharcoal,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TODAY'S TOTAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubtleLabel
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TimeFormatUtils.formatDurationShort(todayTotalMs),
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AcidLime
                        )
                        Text(
                            text = "recorded time",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = TextSubtleLabel
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SEGMENTED PILL SWITCHER BELOW CHART (1d 1w 1m [6m] 1y in reference image)
// ============================================================================

@Composable
private fun SegmentedChartSwitcher(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    val modes = remember {
        listOf("7d Trend", "Top Chats", "Sessions", "Stats")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(26.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEachIndexed { index, title ->
                val isSelected = selectedMode == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .then(
                            if (isSelected) {
                                Modifier.background(AcidLime)
                            } else {
                                Modifier.clickable { onModeSelected(index) }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) AcidLimeInk else TextSubtleLabel
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. QUICK SUMMARY STRIP (Active Chats, Total Sessions, Peak Chat)
// ============================================================================

@Composable
private fun QuickSummaryStrip(
    activeChatsCount: Int,
    totalSessionsCount: Int,
    topChatDisplayName: String,
    onTopChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Column 1: Active Chats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ACTIVE CHATS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtleLabel
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$activeChatsCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
            }

            // Column 2: Total Sessions
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SESSIONS",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtleLabel
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$totalSessionsCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite
                )
            }

            // Column 3: Peak Chat
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .clickable(onClick = onTopChatClick),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "PEAK CHAT",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtleLabel
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = topChatDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = PeriwinkleViolet,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================================================
// 7. RANKED CONVERSATION ROW ITEM (Shopping / 46 transactions / 8.500 00 SOL)
// ============================================================================

@Composable
private fun ConversationRowItem(
    chat: ChatSummary,
    index: Int,
    onClick: () -> Unit
) {
    val chatColor = remember(chat.chatName, chat.colorTag) {
        getChatAccentColor(chat.chatName, chat.colorTag)
    }
    val twoTone = remember(chat.durationMs) {
        splitDurationTwoToneShort(chat.durationMs)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
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
            // Left: 44dp Circular dark button with chat avatar initial / accent ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CircleButtonBg)
                    .border(1.dp, chatColor.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.displayName.take(1).uppercase(Locale.getDefault()),
                    style = TextStyle(
                        color = chatColor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            // Middle: Chat display name + session count & percentage subtitle
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = chat.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${chat.sessionCount} sessions • ${chat.percentage.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        color = TextSubtleLabel
                    )
                    if (chat.dailyLimitMinutes != null) {
                        val isOverLimit = chat.durationMs >= (chat.dailyLimitMinutes * 60_000L)
                        Text(
                            text = if (isOverLimit) "• Limit reached" else "• ${chat.dailyLimitMinutes}m limit",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverLimit) SoftCoral else TextSubtleLabel
                        )
                    }
                }
            }

            // Right: Two-tone duration display matching 8.500 00 SOL in reference image!
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = twoTone.primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                if (twoTone.secondary.isNotEmpty()) {
                    Text(
                        text = twoTone.secondary,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMutedGray
                    )
                }
            }
        }
    }
}

// ============================================================================
// EMPTY CONVERSATIONS CARD
// ============================================================================

@Composable
private fun EmptyConversationsCard(
    onSeedDemoData: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceCharcoal,
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No chats recorded yet today",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Text(
                text = "Conversations you open in WhatsApp will automatically appear here with exact screen time.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = TextSubtleLabel,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSeedDemoData,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcidLime,
                    contentColor = AcidLimeInk
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AcidLimeInk
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Load Sample Analytics",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AcidLimeInk
                )
            }
        }
    }
}

// ============================================================================
// STATUS PULSING DOT
// ============================================================================

@Composable
private fun PulsingDot(
    isPulsing: Boolean,
    color: Color,
    size: Dp
) {
    if (isPulsing) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulseDotTransition")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(750),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer { this.alpha = alpha }
                .clip(CircleShape)
                .background(color)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * Clean compact axis label formatter for chart reference lines.
 */
private fun formatChartAxisLabel(durationMs: Long): String {
    if (durationMs <= 0L) return "0m"
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}
