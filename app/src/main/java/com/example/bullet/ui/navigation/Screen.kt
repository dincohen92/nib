package com.example.bullet.ui.navigation

sealed class Screen(val route: String) {
    object DailyLog : Screen("daily_log")
    object Recurring : Screen("recurring")
    object Aspirations : Screen("aspirations")
}
