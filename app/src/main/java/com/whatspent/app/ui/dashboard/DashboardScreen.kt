package com.whatspent.app.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.whatspent.app.ui.theme.getChatColor
import com.whatspent.app.util.PermissionHelper
import com.whatspent.app.util.TimeFormatUtils
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ==========================================
// PALETTE & COLOR CONSTANTS (Reference Image)
// ==========================================
private val ColorObsidianBg = Color(0xFF0B0B0D)
private val ColorDarkCharcoalCard = Color(0xFF1F1F21)
private val ColorDarkCharcoalButton = Color(0xFF262522)
private val ColorDarkBorder = Color(0xFF2C2C2E)

private val ColorButterGold = Color(0xFFEFE29B)
private val ColorButterGoldDark = Color(0xFF141413)
private val ColorGoldMuted = Color(0xFFD9D2B0)

private val ColorTextWhite = Color(0xFFF5F5F7)
private val ColorTextMuted = Color(0xFF8E8E93)
private val ColorTextSubtle = Color(0xFF6E6E73)

private val ColorHeroSage = Color(0xFFBCE0D0)
private val ColorHeroSageSubtext = Color(0xFF566E63)
private val ColorHeroSageBorder = Color(0xFF6E7F77)
private val ColorHeroPillBg = Color(0xFFE8FCE2)

private val ColorTrendSand = Color(0xFFC9BA9B)
private val ColorTrendSandSubtext = Color(0xFF5C5343)
private val ColorTrendSandCallout = Color(0xFFDAF5E6)

private val ColorBarOlive = Color(0xFFCED2BE)
private val ColorBarOliveSubtext = Color(0xFF4A4E40)
private val ColorCapsulePeach = Color(0xFFF3B282)

private val ColorSummarySage = Color(0xFFCBD1C4)
private val ColorSummarySageSubtext = Color(0xFF565E52)

private val ColorBadgeGreen = Color(0xFF8BE8B9)
private val ColorBadgeGreenDark = Color(0xFF0D2618)
private val ColorCoralRed = Color(0xFFFF6464)

// Arch Gauge Colors
private val ArchColors = listOf(
    Color(0xFF63C89A), // Mint Emerald
    Color(0xFFAEF075), // Lime Green
    Color(0xFFFFB978), // Warm Peach
    Color(0xFFFF8A7A), // Coral
    Color(0xFFA5B4FC)  // Periwinkle
)

// Pastel Pill Rows for Conversation Summary
private val PastelRowColors = listOf(
    Color(0xFFDCE2F9), // Lavender
    Color(0xFFCBEAF6), // Sky
    Color(0xFFC6F0DC), // Mint
    Color(0xFFF7F2CD)  // Cream
)

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

    // 0: Overview, 1: Analytics, 2: Chats
    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ColorObsidianBg
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ColorButterGold)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Header Row
                item(key = "top_header") {
                    DashboardTopHeader(
                        isCoreTrackingReady = permissionStatus.isCoreTrackingReady,
                        isTracking = uiState.activeSession.isTracking,
                        onOpenSettings = onNavigateToSettings,
                        onOpenOnboarding = onNavigateToOnboarding
                    )
                }

                // 2. Action Bar Row (Search toggle, indicators, Butter-Gold action)
                item(key = "action_bar") {
                    DashboardActionBar(
                        isSearchOpen = isSearchOpen,
                        onToggleSearch = { isSearchOpen = !isSearchOpen },
                        onPrimaryAction = onSeedDemoData
                    )
                }

                // 2.1 Inline Search Bar (Animated)
                item(key = "inline_search_bar") {
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

                // 3. Numbered Tab Strip (01 Overview, 02 Analytics, 03 Chats)
                item(key = "numbered_tab_strip") {
                    NumberedTabStrip(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it }
                    )
                }

                // 4. Live Tracking Banner (when active)
                if (uiState.activeSession.isTracking) {
                    item(key = "live_tracking_banner") {
                        LiveTrackingCharcoalCard(
                            activeSession = uiState.activeSession,
                            onClick = {
                                uiState.activeSession.currentChatName?.let { onNavigateToChatDetail(it) }
                            }
                        )
                    }
                }

                // ==========================================
                // CONDITIONAL CONTENT BASED ON NUMBERED TAB
                // ==========================================

                // Overview Tab (0) or Chats Tab (2): Show Hero Arch Gauge
                if (selectedTab == 0 || selectedTab == 2) {
                    item(key = "hero_squircle_card") {
                        HeroSageSquircleCard(
                            uiState = uiState,
                            onChatClick = { onNavigateToChatDetail(it) }
                        )
                    }
                }

                // Overview Tab (0) or Analytics Tab (1): Show Twin Bento Cards
                if (selectedTab == 0 || selectedTab == 1) {
                    item(key = "twin_bento_cards") {
                        TwinDarkBentoCards(
                            uiState = uiState,
                            onTopChatClick = { chatName -> onNavigateToChatDetail(chatName) }
                        )
                    }
                }

                // Overview Tab (0) or Analytics Tab (1): Show Warm Sand 7-Day Bezier Trend Card
                if (selectedTab == 0 || selectedTab == 1) {
                    item(key = "warm_sand_bezier_trend") {
                        WarmSandBezierTrendCard(
                            points = uiState.sevenDayUsage
                        )
                    }
                }

                // Overview Tab (0) or Analytics Tab (1): Show Soft Olive Capsule Bar Card
                if (selectedTab == 0 || selectedTab == 1) {
                    item(key = "soft_olive_capsule_bar") {
                        SoftOliveCapsuleBarCard(
                            points = uiState.sevenDayUsage
                        )
                    }
                }

                // Overview Tab (0) or Chats Tab (2): Show Soft Sage Summary Card (Ranked Conversations)
                if (selectedTab == 0 || selectedTab == 2) {
                    item(key = "soft_sage_summary_card") {
                        SoftSageSummaryCard(
                            chatSummaries = filteredChats,
                            onChatClick = onNavigateToChatDetail,
                            onSeedDemoData = onSeedDemoData
                        )
                    }
                }

                // Bottom spacer for comfortable navigation clearance
                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

// ==========================================
// 1. TOP HEADER ROW
// ==========================================
@Composable
private fun DashboardTopHeader(
    isCoreTrackingReady: Boolean,
    isTracking: Boolean,
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 4-dot Grid Icon
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onOpenSettings),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GridView,
                contentDescription = "Settings Menu",
                modifier = Modifier.size(24.dp),
                tint = ColorGoldMuted
            )
        }

        // Center: "Dashboard"
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.titleLarge,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorTextWhite
        )

        // Right: Live Status Pill
        if (isCoreTrackingReady) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenSettings),
                color = Color(0xFF16231A),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF23442C))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusIndicatorDot(
                        isPulsing = isTracking,
                        color = ColorBadgeGreen,
                        size = 8.dp
                    )
                    Text(
                        text = if (isTracking) "Tracking" else "Active",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorBadgeGreen
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenOnboarding),
                color = Color(0xFF2B1A14),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5A2A1A))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Color(0xFFFFB978)
                    )
                    Text(
                        text = "Setup Needed",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB978)
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. ACTION BAR ROW
// ==========================================
@Composable
private fun DashboardActionBar(
    isSearchOpen: Boolean,
    onToggleSearch: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 46dp Dark Circular Button with Search
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(ColorDarkCharcoalButton)
                .clickable(onClick = onToggleSearch),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Chats",
                modifier = Modifier.size(20.dp),
                tint = if (isSearchOpen) ColorButterGold else ColorGoldMuted
            )
        }

        // Middle: Subtle Quick Actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = ColorTextMuted
            )
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = ColorTextMuted
            )
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = ColorTextMuted
            )
        }

        // Right: 46dp Butter-Gold Circular Button with +
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(ColorButterGold)
                .clickable(onClick = onPrimaryAction),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Seed Sample Analytics",
                modifier = Modifier.size(22.dp),
                tint = ColorButterGoldDark
            )
        }
    }
}

// ==========================================
// 2.1 INLINE SEARCH BAR
// ==========================================
@Composable
private fun InlineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
        color = ColorDarkCharcoalCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, ColorDarkBorder)
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
                tint = ColorGoldMuted
            )
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search conversations...",
                        style = TextStyle(
                            color = ColorTextMuted,
                            fontSize = 14.sp
                        )
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = ColorTextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(ColorButterGold),
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
                    tint = ColorTextMuted
                )
            }
        }
    }
}

// ==========================================
// 3. NUMBERED TAB STRIP (01, 02, 03)
// ==========================================
private data class DashboardTabItem(
    val number: String,
    val title: String
)

@Composable
private fun NumberedTabStrip(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = remember {
        listOf(
            DashboardTabItem("01", "Overview"),
            DashboardTabItem("02", "Analytics"),
            DashboardTabItem("03", "Chats")
        )
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(tabs) { index, tab ->
            val isSelected = selectedTab == index
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) ColorButterGold else Color(0xFF3A3A3E),
                label = "tabBorderColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) ColorTextWhite else ColorTextMuted,
                label = "tabTextColor"
            )
            val numberColor by animateColorAsState(
                targetValue = if (isSelected) ColorButterGold else ColorTextMuted,
                label = "tabNumberColor"
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTabSelected(index) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Circular numbered badge
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .border(1.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab.number,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = numberColor
                    )
                }

                // Tab label
                Text(
                    text = tab.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                )
            }
        }
    }
}

// ==========================================
// 4. LIVE TRACKING BANNER
// ==========================================
@Composable
private fun LiveTrackingCharcoalCard(
    activeSession: ActiveSessionState,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = ColorDarkCharcoalCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E4D3C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                StatusIndicatorDot(
                    isPulsing = true,
                    color = ColorBadgeGreen,
                    size = 12.dp
                )
                Column {
                    Text(
                        text = "NOW ACTIVE IN WHATSAPP",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorBadgeGreen,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = activeSession.currentDisplayName ?: "Conversation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDurationWithSeconds(activeSession.currentSessionDurationMs),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorBadgeGreen
                )
                Text(
                    text = "Today: ${TimeFormatUtils.formatDurationShort(activeSession.currentChatTodayTotalMs)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ColorTextMuted
                )
            }
        }
    }
}

// ==========================================
// 6. HERO SQUIRCLE CARD — SAGE-MINT
// ==========================================
@Composable
private fun HeroSageSquircleCard(
    uiState: DashboardUiState,
    onChatClick: (String) -> Unit
) {
    val topChats = remember(uiState.chatSummaries) {
        uiState.chatSummaries.filter { !it.isExcluded && it.durationMs > 0L }.take(5)
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    // Bounds check whenever topChats changes
    val safeSelectedIndex = if (topChats.isNotEmpty()) selectedIndex.coerceIn(0, topChats.size - 1) else 0
    val activeChat = topChats.getOrNull(safeSelectedIndex)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = ColorHeroSage
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: "Top 5 chats by spend" + Circular ↗ button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Top 5 chats by spend",
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF121413)
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, ColorHeroSageBorder, CircleShape)
                        .clickable {
                            activeChat?.let { onChatClick(it.chatName) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = "Inspect Chat",
                        modifier = Modifier.size(18.dp),
                        tint = Color(0xFF121413)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: "Total time today"
            Text(
                text = "Total time today",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ColorHeroSageSubtext
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Giant Hero Time Display (e.g. "2h 45m" or "2,985")
            Text(
                text = TimeFormatUtils.formatDurationShort(uiState.todayTotalMs),
                style = MaterialTheme.typography.displayMedium,
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF111312)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 180° Semi-Circular Arch Gauge
            SemiCircularArchGauge(
                topChats = topChats,
                selectedIndex = safeSelectedIndex,
                onSegmentClick = { index -> selectedIndex = index }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Nested Floating Mint Callout Pill
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = ColorHeroPillBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            activeChat?.let { onChatClick(it.chatName) }
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 26.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (activeChat != null) {
                                TimeFormatUtils.formatDurationShort(activeChat.durationMs)
                            } else {
                                "0m"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF121413)
                        )
                        Text(
                            text = activeChat?.displayName ?: "No chats yet",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A6056),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pager Dots (• • •) for Top Chats
            if (topChats.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    topChats.indices.forEach { index ->
                        val isCurrent = index == safeSelectedIndex
                        val dotScale by animateFloatAsState(
                            targetValue = if (isCurrent) 1.25f else 1.0f,
                            label = "pagerDotScale_$index"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (isCurrent) 14.dp else 6.dp, 6.dp)
                                .graphicsLayer {
                                    scaleX = dotScale
                                    scaleY = dotScale
                                }
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (isCurrent) Color(0xFF121413) else Color(0xFF121413).copy(alpha = 0.25f)
                                )
                                .clickable { selectedIndex = index }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 180° Semi-Circular Arch Gauge with segmented slices and 2.5-degree gaps.
 */
@Composable
private fun SemiCircularArchGauge(
    topChats: List<ChatSummary>,
    selectedIndex: Int,
    onSegmentClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMs = remember(topChats) {
        topChats.sumOf { it.durationMs }.coerceAtLeast(1L)
    }

    // Animation progress keyed on top chats
    val animProgress = remember { Animatable(1f) }
    val chatsKey = remember(topChats) { topChats.map { it.chatName } }
    LaunchedEffect(chatsKey) {
        if (topChats.isNotEmpty()) {
            animProgress.snapTo(0.2f)
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(topChats, totalMs) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height - 12.dp.toPx())
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        val strokePx = 28.dp.toPx()
                        val outerRadius = (size.width / 2f) - (strokePx / 2f)
                        val innerRadius = outerRadius - strokePx

                        // Check if tap falls in the arch ring
                        if (dist in (innerRadius * 0.7f)..(outerRadius * 1.3f) && dy <= 0f) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            // angle is between -180 and 0. Normalize to 0..180 where -180 is 0
                            val normalizedAngle = angle + 180f

                            var currentStart = 0f
                            val gap = if (topChats.size > 1) 2.5f else 0f
                            val totalUsable = 180f - (gap * (topChats.size - 1))

                            topChats.forEachIndexed { i, chat ->
                                val ratio = chat.durationMs.toFloat() / totalMs.toFloat()
                                val sweep = ratio * totalUsable
                                if (normalizedAngle in currentStart..(currentStart + sweep + gap)) {
                                    onSegmentClick(i)
                                    return@detectTapGestures
                                }
                                currentStart += sweep + gap
                            }
                        }
                    }
                }
        ) {
            val strokeWidthPx = 28.dp.toPx()
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Arch center is at the bottom-center of canvas
            val center = Offset(canvasWidth / 2f, canvasHeight - 12.dp.toPx())
            val radius = (canvasWidth / 2f) - (strokeWidthPx / 2f) - 6.dp.toPx()
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            if (topChats.isEmpty()) {
                // Placeholder track
                drawArc(
                    color = Color(0xFF98B8A8).copy(alpha = 0.45f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            } else {
                val progress = animProgress.value
                val gap = if (topChats.size > 1) 2.5f else 0f
                val totalUsableSweep = 180f - (gap * (topChats.size - 1))

                var currentAngle = 180f
                topChats.forEachIndexed { index, chat ->
                    val color = ArchColors.getOrElse(index) { Color(0xFF63C89A) }
                    val ratio = (chat.durationMs.toFloat() / totalMs.toFloat()).coerceIn(0.05f, 1f)
                    val sweep = (ratio * totalUsableSweep * progress).coerceAtLeast(3f)
                    val isSelected = index == selectedIndex
                    val currentStroke = if (isSelected) strokeWidthPx + 4.dp.toPx() else strokeWidthPx

                    drawArc(
                        color = color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(
                            width = currentStroke,
                            cap = StrokeCap.Butt
                        )
                    )
                    currentAngle += sweep + gap
                }
            }
        }
    }
}

// ==========================================
// 7. TWIN DARK BENTO CARDS
// ==========================================
@Composable
private fun TwinDarkBentoCards(
    uiState: DashboardUiState,
    onTopChatClick: (String) -> Unit
) {
    val topChat = remember(uiState.chatSummaries) {
        uiState.chatSummaries.firstOrNull { !it.isExcluded }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Bento Card: Daily Spend
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            color = ColorDarkCharcoalCard
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
                    Text(
                        text = "Daily Spend",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextWhite
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NorthEast,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = ColorGoldMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = TimeFormatUtils.formatReadableDate(TimeFormatUtils.todayDateString()),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = ColorTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = TimeFormatUtils.formatDurationShort(uiState.todayTotalMs),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorTextWhite
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom pill badge: +15% vs yesterday
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ColorBadgeGreen
                ) {
                    Text(
                        text = uiState.comparisonText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorBadgeGreenDark
                    )
                }
            }
        }

        // Right Bento Card: Peak Chat
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(28.dp))
                .clickable {
                    topChat?.let { onTopChatClick(it.chatName) }
                },
            shape = RoundedCornerShape(28.dp),
            color = ColorDarkCharcoalCard
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
                    Text(
                        text = "Peak Chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextWhite
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NorthEast,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = ColorGoldMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Today's leader",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 12.sp,
                    color = ColorTextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Coral Highlight Text for Peak Duration
                Text(
                    text = if (topChat != null) TimeFormatUtils.formatDurationShort(topChat.durationMs) else "0m",
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ColorCoralRed
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = topChat?.displayName ?: "None",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD1D1D6),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// 8. WARM SAND 7-DAY BEZIER TREND CARD
// ==========================================
@Composable
private fun WarmSandBezierTrendCard(
    points: List<DailyUsagePoint>
) {
    val displayPoints = remember(points) {
        if (points.isNotEmpty()) {
            points.takeLast(7)
        } else {
            // Generate fallback 7 days
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

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxDuration = remember(displayPoints) {
        val maxVal = displayPoints.maxOfOrNull { it.totalDurationMs } ?: 0L
        if (maxVal <= 0L) 3600_000L else maxVal
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = ColorTrendSand
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: "7-Day Screen Time" + ↗ button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "7-Day Screen Time",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF141615)
                    )
                    Text(
                        text = "Last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = ColorTrendSandSubtext
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF5C5343), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF141615)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Cubic Bezier Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(displayPoints) {
                            detectTapGestures { tapOffset ->
                                val slotW = size.width / displayPoints.size
                                val tapped = (tapOffset.x / slotW).toInt()
                                if (tapped in displayPoints.indices) {
                                    selectedIndex = if (selectedIndex == tapped) null else tapped
                                }
                            }
                        }
                ) {
                    val count = displayPoints.size
                    if (count < 2) return@Canvas

                    val availableHeight = size.height - 28.dp.toPx()
                    val availableWidth = size.width
                    val slotWidth = availableWidth / (count - 1).coerceAtLeast(1)

                    // Draw 3 horizontal subtle grid lines
                    val gridColor = Color(0xFF181715).copy(alpha = 0.12f)
                    for (i in 1..3) {
                        val y = (availableHeight / 4f) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(availableWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Compute points coordinates
                    val pointOffsets = displayPoints.mapIndexed { index, item ->
                        val x = index * slotWidth
                        val ratio = (item.totalDurationMs.toFloat() / maxDuration.toFloat()).coerceIn(0.08f, 0.95f)
                        val y = availableHeight * (1f - ratio) + 8.dp.toPx()
                        Offset(x, y)
                    }

                    // Draw smooth cubic Bezier path
                    val path = Path().apply {
                        moveTo(pointOffsets[0].x, pointOffsets[0].y)
                        for (i in 0 until pointOffsets.size - 1) {
                            val p0 = pointOffsets[i]
                            val p1 = pointOffsets[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = Color(0xFF181715),
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw circular dots at each point
                    pointOffsets.forEachIndexed { index, offset ->
                        val isSelected = index == (selectedIndex ?: (count - 1))
                        drawCircle(
                            color = if (isSelected) Color(0xFF181715) else Color(0xFF181715).copy(alpha = 0.6f),
                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                            center = offset
                        )
                        if (isSelected) {
                            drawCircle(
                                color = ColorTrendSand,
                                radius = 2.dp.toPx(),
                                center = offset
                            )
                        }
                    }
                }

                // Floating Mint Callout Pill over selected or latest point
                val activeIndex = selectedIndex ?: (displayPoints.size - 1)
                val activePoint = displayPoints.getOrNull(activeIndex)
                if (activePoint != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = ColorTrendSandCallout,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBCE0D0))
                        ) {
                            Text(
                                text = "${activePoint.dayLabel}: ${TimeFormatUtils.formatDurationShort(activePoint.totalDurationMs)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF141615)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day labels Mon..Sun
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayPoints.forEachIndexed { index, point ->
                    val isSelected = index == (selectedIndex ?: (displayPoints.size - 1))
                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF141615) else Color(0xFF3C3528)
                    )
                }
            }
        }
    }
}

// ==========================================
// 9. SOFT OLIVE CAPSULE BAR CARD
// ==========================================
@Composable
private fun SoftOliveCapsuleBarCard(
    points: List<DailyUsagePoint>
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

    val maxDuration = remember(displayPoints) {
        val maxVal = displayPoints.maxOfOrNull { it.totalDurationMs } ?: 0L
        if (maxVal <= 0L) 3600_000L else maxVal
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = ColorBarOlive
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: "Daily vs Average" + ↗ button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily vs Average",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF141615)
                    )
                    Text(
                        text = "Last 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = ColorBarOliveSubtext
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF6E7260), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF141615)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ColorCapsulePeach)
                    )
                    Text(
                        text = "Daily",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        color = Color(0xFF141615)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ColorButterGold)
                    )
                    Text(
                        text = "Target / Avg",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        color = Color(0xFF141615)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Capsule Bar Canvas with connected line overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val count = displayPoints.size
                    val availableH = size.height - 24.dp.toPx()
                    val slotW = size.width / count.coerceAtLeast(1)
                    val barW = (slotW * 0.48f).coerceIn(16.dp.toPx(), 32.dp.toPx())

                    val targetPoints = mutableListOf<Offset>()

                    // 1. Draw rounded capsule bars
                    displayPoints.forEachIndexed { i, point ->
                        val centerX = (i * slotW) + (slotW / 2f)
                        val left = centerX - (barW / 2f)
                        val ratio = (point.totalDurationMs.toFloat() / maxDuration.toFloat()).coerceIn(0.12f, 0.95f)
                        val barH = availableH * ratio
                        val top = availableH - barH

                        drawRoundRect(
                            color = ColorCapsulePeach.copy(alpha = 0.85f),
                            topLeft = Offset(left, top),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )

                        // Compute target line node slightly above the bar
                        val targetY = (top * 0.9f).coerceAtLeast(10.dp.toPx())
                        targetPoints.add(Offset(centerX, targetY))
                    }

                    // 2. Draw connected line overlay o—o
                    for (i in 0 until targetPoints.size - 1) {
                        drawLine(
                            color = Color(0xFF181715),
                            start = targetPoints[i],
                            end = targetPoints[i + 1],
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // 3. Draw circle nodes on the target line
                    targetPoints.forEach { pt ->
                        drawCircle(
                            color = Color(0xFF181715),
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = ColorBarOlive,
                            radius = 2.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayPoints.forEach { point ->
                    Text(
                        text = point.dayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF383C2F)
                    )
                }
            }
        }
    }
}

// ==========================================
// 10. SOFT SAGE SUMMARY CARD (Pastel Pills)
// ==========================================
@Composable
private fun SoftSageSummaryCard(
    chatSummaries: List<ChatSummary>,
    onChatClick: (String) -> Unit,
    onSeedDemoData: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = ColorSummarySage
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            // Header Row: "Summary" + ↗ button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF141615)
                    )
                    Text(
                        text = "Today's conversations",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 13.sp,
                        color = ColorSummarySageSubtext
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFF6E7265), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NorthEast,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF141615)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (chatSummaries.isEmpty()) {
                // Empty state within summary card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No chats recorded yet today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4C5449),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSeedDemoData,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF141615),
                            contentColor = ColorButterGold
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Load Sample Analytics",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Pastel Horizontal Pill Rows
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    chatSummaries.forEachIndexed { index, chat ->
                        val pillBgColor = PastelRowColors[index % PastelRowColors.size]
                        val chatColor = getChatColor(chat.chatName, chat.colorTag)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { onChatClick(chat.chatName) },
                            shape = RoundedCornerShape(20.dp),
                            color = pillBgColor
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Colored ring dot + Title + Session count
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Colored ring dot
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .border(2.5.dp, chatColor, CircleShape)
                                    )

                                    Column(verticalArrangement = Arrangement.Center) {
                                        Text(
                                            text = chat.displayName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF141615),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${chat.sessionCount} ${if (chat.sessionCount == 1) "session" else "sessions"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 11.sp,
                                            color = Color(0xFF4C5449)
                                        )
                                    }
                                }

                                // Right: Crisp White Rounded Pill with Duration
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = TimeFormatUtils.formatDurationShort(chat.durationMs),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF141615)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// STATUS PULSING DOT
// ==========================================
@Composable
private fun StatusIndicatorDot(
    isPulsing: Boolean,
    color: Color,
    size: Dp
) {
    if (isPulsing) {
        val infiniteTransition = rememberInfiniteTransition(label = "statusDotPulse")
        val pulseAlpha = infiniteTransition.animateFloat(
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
                .graphicsLayer { alpha = pulseAlpha.value }
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
