package com.stillwellbeing.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stillwellbeing.app.ui.insights.InsightsScreen
import com.stillwellbeing.app.ui.settings.SettingsScreen
import com.stillwellbeing.app.ui.theme.LocalStillPalette
import com.stillwellbeing.app.ui.today.TodayScreen

private sealed class Destination(val route: String, val label: String) {
    data object Today : Destination("today", "Today")
    data object Insights : Destination("insights", "Insights")
    data object Settings : Destination("settings", "Settings")

    companion object {
        val all = listOf(Today, Insights, Settings)
    }
}

@Composable
fun StillNavHost() {
    val navController = rememberNavController()
    val palette = LocalStillPalette.current

    Scaffold(
        containerColor = palette.paper,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination

            NavigationBar(containerColor = palette.paper) {
                Destination.all.forEach { destination ->
                    val selected = currentRoute?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon(), contentDescription = null) },
                        label = { Text(destination.label) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = palette.ink,
                            selectedTextColor = palette.ink,
                            indicatorColor = palette.sage,
                            unselectedIconColor = palette.muted,
                            unselectedTextColor = palette.muted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Today.route) {
                TodayScreen(onManageGoals = {
                    navController.navigate(Destination.Settings.route) {
                        launchSingleTop = true
                    }
                })
            }
            composable(Destination.Insights.route) { InsightsScreen() }
            composable(Destination.Settings.route) { SettingsScreen() }
        }
    }
}

private fun Destination.icon() = when (this) {
    Destination.Today -> Icons.Outlined.Home
    Destination.Insights -> Icons.Outlined.BarChart
    Destination.Settings -> Icons.Outlined.Settings
}
