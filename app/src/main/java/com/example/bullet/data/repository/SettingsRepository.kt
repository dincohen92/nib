package com.example.bullet.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("nib_settings", Context.MODE_PRIVATE)

    var pushedTasksNotification: Boolean
        get() = prefs.getBoolean("pushed_notif", true)
        set(value) = prefs.edit().putBoolean("pushed_notif", value).apply()

    var dailyReminder: Boolean
        get() = prefs.getBoolean("daily_reminder", false)
        set(value) = prefs.edit().putBoolean("daily_reminder", value).apply()
}
