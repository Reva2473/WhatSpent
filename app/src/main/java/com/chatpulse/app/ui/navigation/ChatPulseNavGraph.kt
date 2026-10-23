package com.chatpulse.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chatpulse.app.data.repository.ChatPulseRepository
import com.chatpulse.app.ui.dashboard.DashboardScreen
import com.chatpulse.app.ui.dashboard.DashboardViewModel
import com.chatpulse.app.ui.detail.ChatDetailScreen
import com.chatpulse.app.ui.detail.ChatDetailViewModel
import com.chatpulse.app.ui.onboarding.OnboardingScreen
import com.chatpulse.app.ui.settings.SettingsScreen
import com.chatpulse.app.ui.settings.SettingsViewModel
import com.chatpulse.app.ui.timeline.TimelineScreen
import com.chatpulse.app.ui.timeline.TimelineViewModel
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
fun ChatPulseNavGraph(
    repository: ChatPulseRepository,
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = MaterialTheme.colorScheme.surfaceTint.let { 3.dp }
                ) {
                    BottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
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
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon!! else screen.unselectedIcon!!,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
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
