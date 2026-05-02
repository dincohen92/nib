package com.example.bullet.ui.navigation

sealed class Screen(val route: String) {
    object DailyLog : Screen("daily_log")
    object Journal : Screen("journal")
    object Recurring : Screen("recurring")
    object Aspirations : Screen("aspirations")
    object DayView : Screen("day_view")
    object Settings : Screen("settings")
}
