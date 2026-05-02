package com.example.bullet.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bullet.ui.aspirations.AspirationsScreen
import com.example.bullet.ui.calendar.CalendarScreen
import com.example.bullet.ui.journal.JournalScreen
import com.example.bullet.ui.recurring.RecurringScreen
import com.example.bullet.ui.settings.SettingsScreen
import com.example.bullet.ui.today.TodayScreen

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.DailyLog, "Tasks", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    BottomNavItem(Screen.Journal, "Journal", Icons.Filled.AutoStories, Icons.Outlined.AutoStories),
    BottomNavItem(Screen.Recurring, "Habits", Icons.Filled.Loop, Icons.Outlined.Loop),
    BottomNavItem(Screen.Aspirations, "Aspirations", Icons.Filled.Star, Icons.Outlined.StarOutline),
    BottomNavItem(Screen.DayView, "Calendar", Icons.Filled.DateRange, Icons.Outlined.DateRange),
)

@Composable
fun NibApp() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { NibBottomNav(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.DailyLog.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.DailyLog.route) {
                TodayScreen(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.Journal.route) {
                JournalScreen(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.Recurring.route) {
                RecurringScreen(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.Aspirations.route) {
                AspirationsScreen(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.DayView.route) {
                CalendarScreen(onSettingsClick = { navController.navigate(Screen.Settings.route) })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
private fun NibBottomNav(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.screen.route
                NibNavItem(
                    item = item,
                    selected = selected,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                )
            }
        }
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

@Composable
private fun NibNavItem(
    item: BottomNavItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected)
        MaterialTheme.colorScheme.onSurface
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)

    Column(
        modifier = modifier.padding(top = 12.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            modifier = Modifier.size(24.dp),
            tint = contentColor,
        )
        Text(
            text = item.label,
            fontSize = 10.sp,
            letterSpacing = 0.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
