package com.whatspent.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.whatspent.app.data.repository.WhatSpentRepository
import com.whatspent.app.ui.dashboard.DashboardScreen
import com.whatspent.app.ui.dashboard.DashboardViewModel
import com.whatspent.app.ui.detail.ChatDetailScreen
import com.whatspent.app.ui.detail.ChatDetailViewModel
import com.whatspent.app.ui.onboarding.OnboardingScreen
import com.whatspent.app.ui.settings.SettingsScreen
import com.whatspent.app.ui.settings.SettingsViewModel
import com.whatspent.app.ui.theme.AcidLimeAccent
import com.whatspent.app.ui.theme.CharcoalBentoSurface
import com.whatspent.app.ui.theme.ObsidianBackground
import com.whatspent.app.ui.theme.OnAcidLimeInk
import com.whatspent.app.ui.theme.SubtleAxisLabelText
import com.whatspent.app.ui.timeline.TimelineScreen
import com.whatspent.app.ui.timeline.TimelineViewModel
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(
    val route: String,
    val title: String = "",
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    object Timeline : Screen("timeline", "Timeline", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object Onboarding : Screen("onboarding", "Setup")
    object ChatDetail : Screen("chat_detail/{chatName}") {
        fun createRoute(chatName: String): String {
            val encoded = URLEncoder.encode(chatName, StandardCharsets.UTF_8.toString())
            return "chat_detail/$encoded"
        }
    }
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Timeline,
    Screen.Settings
)

@Composable
fun WhatSpentNavGraph(
    repository: WhatSpentRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scope = rememberCoroutineScope()

    val initialOnboarding = remember { repository.preferences.isOnboardingCompletedSync() }
    val isOnboardingCompleted by repository.preferences.onboardingCompleted.collectAsState(initial = initialOnboarding)

    val startDestination = if (isOnboardingCompleted) {
        Screen.Dashboard.route
    } else {
        Screen.Onboarding.route
    }

    val showBottomBar = currentRoute in BottomNavItems.map { it.route }

    // Outer Scaffold with zero insets to prevent double-inset blank spaces with nested screens
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                // Sleek, ultra-compact dark pill dock container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBackground)
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = CharcoalBentoSurface,
                        border = BorderStroke(1.dp, Color(0xFF25252A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BottomNavItems.forEach { screen ->
                                val isSelected = currentRoute == screen.route
                                val animatedBgColor by animateColorAsState(
                                    targetValue = if (isSelected) AcidLimeAccent else Color.Transparent,
                                    label = "navTabBg_${screen.route}"
                                )
                                val contentColor by animateColorAsState(
                                    targetValue = if (isSelected) OnAcidLimeInk else SubtleAxisLabelText,
                                    label = "navTabContent_${screen.route}"
                                )

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(animatedBgColor)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) {
                                                screen.selectedIcon ?: Icons.Filled.Dashboard
                                            } else {
                                                screen.unselectedIcon ?: Icons.Outlined.Dashboard
                                            },
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(18.dp),
                                            tint = contentColor
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = screen.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = contentColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Content starts flush right below status bar clock/battery icons with zero wasted gap
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Dashboard
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModel.provideFactory(repository)
                )
                val uiState by viewModel.uiState.collectAsState()

                DashboardScreen(
                    uiState = uiState,
                    onNavigateToChatDetail = { chatName ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatName))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    },
                    onSeedDemoData = {
                        viewModel.seedDemoData()
                    }
                )
            }

            // Timeline
            composable(Screen.Timeline.route) {
                val viewModel: TimelineViewModel = viewModel(
                    factory = TimelineViewModel.provideFactory(repository)
                )
                val selectedDate by viewModel.selectedDate.collectAsState()
                val availableDates by viewModel.availableDates.collectAsState()
                val sessions by viewModel.sessions.collectAsState()

                TimelineScreen(
                    selectedDate = selectedDate,
                    availableDates = availableDates,
                    sessions = sessions,
                    onSelectDate = { date -> viewModel.selectDate(date) },
                    onNavigateToChatDetail = { chatName ->
                        navController.navigate(Screen.ChatDetail.createRoute(chatName))
                    }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(repository)
                )
                val floatingPillEnabled by viewModel.floatingPillEnabled.collectAsState()
                val biometricLockEnabled by viewModel.biometricLockEnabled.collectAsState()
                val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()

                SettingsScreen(
                    floatingPillEnabled = floatingPillEnabled,
                    biometricLockEnabled = biometricLockEnabled,
                    dynamicColorEnabled = dynamicColorEnabled,
                    onToggleFloatingPill = { viewModel.setFloatingPillEnabled(it) },
                    onToggleBiometricLock = { viewModel.setBiometricLockEnabled(it) },
                    onToggleDynamicColor = { viewModel.setDynamicColorEnabled(it) },
                    onSeedDemoData = { viewModel.seedDemoData() },
                    onClearAllData = { viewModel.clearAllData() },
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    }
                )
            }

            // Chat Detail
            composable(
                route = Screen.ChatDetail.route,
                arguments = listOf(
                    navArgument("chatName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val rawChatName = backStackEntry.arguments?.getString("chatName").orEmpty()
                val chatName = remember(rawChatName) {
                    try {
                        URLDecoder.decode(rawChatName, StandardCharsets.UTF_8.toString())
                    } catch (e: Exception) {
                        rawChatName
                    }
                }

                val viewModel: ChatDetailViewModel = viewModel(
                    key = "chat_detail_$chatName",
                    factory = ChatDetailViewModel.provideFactory(chatName, repository)
                )
                val uiState by viewModel.uiState.collectAsState()

                ChatDetailScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onSelectRange = { range -> viewModel.selectRange(range) },
                    onUpdateDailyLimit = { limit -> viewModel.updateDailyLimit(limit) },
                    onToggleExcluded = { excluded -> viewModel.toggleExcluded(excluded) },
                    onUpdateAlias = { alias -> viewModel.updateCustomAlias(alias) },
                    onUpdateColorTag = { colorArgb -> viewModel.updateColorTag(colorArgb) }
                )
            }

            // Onboarding Setup Wizard
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onCompleteOnboarding = {
                        scope.launch {
                            repository.preferences.setOnboardingCompleted(true)
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    }
}
