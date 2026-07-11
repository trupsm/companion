package com.companion.learning.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.ui.home.HomeScreen
import com.companion.learning.ui.create.CreateRoadmapScreen
import com.companion.learning.ui.topic.TodayTopicScreen
import com.companion.learning.ui.quiz.QuizScreen
import com.companion.learning.ui.notes.NotesScreen
import com.companion.learning.ui.settings.SettingsScreen
import com.companion.learning.ui.importroadmap.ImportRoadmapScreen
import com.companion.learning.ui.details.RoadmapDetailsScreen
import com.companion.learning.ui.dashboard.DashboardScreen
import com.companion.learning.ui.auth.AuthScreen
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Home)
    object Roadmaps : Screen("home", "Roadmaps", Icons.Default.Book)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val secureStorage = remember { SecureStorage(context) }
    val isLoggedIn = remember { secureStorage.getUsername() != "Learner" }
    val startDestination = if (isLoggedIn) "dashboard" else "auth"

    val items = listOf(
        Screen.Dashboard,
        Screen.Roadmaps,
        Screen.Settings
    )

    val showBottomBar = items.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
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
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("dashboard") {
                DashboardScreen(
                    onTaskClick = { taskId -> navController.navigate("topic/$taskId?goal=") }
                )
            }
            composable("home") {
                HomeScreen(
                    onCreateRoadmapClick = { navController.navigate("create") },
                    onRoadmapClick = { id -> navController.navigate("roadmap_details/$id") },
                    onSettingsClick = { navController.navigate("settings") },
                    onImportClick = { navController.navigate("import") }
                )
            }
            composable("create") {
                CreateRoadmapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            // topic/{id}?goal={goal} — optional goal query param for resource recommendations
            composable(
                route = "topic/{id}?goal={goal}",
                arguments = listOf(
                    navArgument("id") { type = NavType.StringType },
                    navArgument("goal") {
                        type = NavType.StringType
                        defaultValue = ""
                        nullable = true
                    }
                )
            ) { backStack ->
                val rawGoal = backStack.arguments?.getString("goal") ?: ""
                val goal = try { URLDecoder.decode(rawGoal, "UTF-8") } catch (_: Exception) { rawGoal }
                val topicId = backStack.arguments?.getString("id") ?: ""
                TodayTopicScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onOpenNotes = { navController.navigate("notes") },
                    onTakeQuiz = { navController.navigate("quiz/$topicId") },
                    onMarkComplete = { navController.popBackStack() },
                    roadmapGoal = goal
                )
            }
            composable(
                route = "quiz/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) {
                QuizScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("notes") {
                NotesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("import") {
                ImportRoadmapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("roadmap_details/{id}") {
                RoadmapDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTopicClick = { topicId, roadmapGoal ->
                        val encodedGoal = URLEncoder.encode(roadmapGoal, "UTF-8")
                        navController.navigate("topic/$topicId?goal=$encodedGoal")
                    }
                )
            }
        }
    }
}
