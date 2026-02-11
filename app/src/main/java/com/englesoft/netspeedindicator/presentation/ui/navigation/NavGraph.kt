package com.englesoft.netspeedindicator.presentation.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.englesoft.netspeedindicator.presentation.ui.screen.HistoryScreen
import com.englesoft.netspeedindicator.presentation.ui.screen.HomeScreen
import com.englesoft.netspeedindicator.presentation.ui.screen.SettingsScreen

/**
 * Navigation routes
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.List)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

/**
 * Main navigation setup with bottom navigation bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.History.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.History.route) {
            HistoryScreen(
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
