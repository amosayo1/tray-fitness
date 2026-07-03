package com.gymsync.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gymsync.data.local.TokenManager
import com.gymsync.ui.components.BottomNavTab
import com.gymsync.ui.components.GymSyncBottomNavBar
import com.gymsync.ui.screens.auth.ActivateScreen
import com.gymsync.ui.screens.auth.AuthScreen
import com.gymsync.ui.screens.auth.AuthViewModel
import com.gymsync.ui.screens.home.HomeScreen
import com.gymsync.ui.screens.progress.ProgressScreen
import com.gymsync.ui.screens.chat.ChatScreen
import com.gymsync.ui.screens.settings.SettingsScreen
import com.gymsync.ui.screens.admin.AdminScreen
import com.gymsync.ui.screens.workout.ActiveWorkoutScreen
import com.gymsync.ui.screens.workout.StartWorkoutScreen
import com.gymsync.ui.theme.GymSyncTheme

object Routes {
    const val AUTH = "auth"
    const val ACTIVATE = "activate/{token}"
    const val HOME = "home"
    const val WORKOUT = "workout/{workoutId}"
    const val WORKOUT_START = "workout/start"
    const val CHAT = "chat"
    const val PROGRESS = "progress"
    const val SETTINGS = "settings"
    const val ADMIN = "admin"

    fun activateRoute(token: String) = "activate/$token"
    fun workoutRoute(workoutId: String) = "workout/$workoutId"
}

private val bottomNavRoutes = setOf(
    Routes.HOME, Routes.WORKOUT_START, Routes.PROGRESS, Routes.CHAT, Routes.SETTINGS
)

@Composable
fun GymSyncNavHost(tokenManager: TokenManager? = null) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    var isDarkMode by remember { mutableStateOf(tokenManager?.darkMode ?: true) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    GymSyncTheme(darkTheme = isDarkMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val showBottomBar = currentRoute in bottomNavRoutes

            Scaffold(
                bottomBar = {
                    if (showBottomBar) {
                        GymSyncBottomNavBar(
                            currentRoute = currentRoute,
                            onTabSelected = { tab ->
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    NavHost(
                        navController = navController,
                        startDestination = if (authViewModel.uiState.value.isAuthenticated) Routes.HOME else Routes.AUTH,
                        enterTransition = { slideInHorizontally { it / 4 } + fadeIn() },
                        exitTransition = { slideOutHorizontally { -it / 4 } + fadeOut() },
                        popEnterTransition = { slideInHorizontally { -it / 4 } + fadeIn() },
                        popExitTransition = { slideOutHorizontally { it / 4 } + fadeOut() }
                    ) {
                        composable(Routes.AUTH) {
                            AuthScreen(
                                onActivate = { token ->
                                    navController.navigate(Routes.activateRoute(token))
                                },
                                onAuthenticated = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.AUTH) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = Routes.ACTIVATE,
                            arguments = listOf(navArgument("token") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val token = backStackEntry.arguments?.getString("token") ?: ""
                            ActivateScreen(
                                token = token,
                                onActivated = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.HOME) {
                            HomeScreen(
                                onStartWorkout = { navController.navigate(Routes.WORKOUT_START) },
                                onChat = { navController.navigate(Routes.CHAT) },
                                onProgress = { navController.navigate(Routes.PROGRESS) },
                                onSettings = { navController.navigate(Routes.SETTINGS) },
                                onAdmin = { navController.navigate(Routes.ADMIN) }
                            )
                        }

                        composable(Routes.WORKOUT_START) {
                            StartWorkoutScreen(
                                onWorkoutStarted = { workoutId ->
                                    navController.navigate(Routes.workoutRoute(workoutId)) {
                                        popUpTo(Routes.HOME)
                                    }
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Routes.WORKOUT,
                            arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val workoutId = backStackEntry.arguments?.getString("workoutId") ?: ""
                            ActiveWorkoutScreen(
                                workoutId = workoutId,
                                onFinish = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.CHAT) {
                            ChatScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Routes.PROGRESS) {
                            ProgressScreen(onBack = { navController.popBackStack() })
                        }

                        composable(Routes.SETTINGS) {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onLogout = {
                                    authViewModel.logout()
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onDarkModeChange = { isDarkMode = it }
                            )
                        }

                        composable(Routes.ADMIN) {
                            AdminScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
