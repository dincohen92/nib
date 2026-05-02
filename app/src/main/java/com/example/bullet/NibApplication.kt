package com.example.bullet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NibApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    "nib_pushed",
                    "Pushed tasks",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
            manager.createNotificationChannel(
                NotificationChannel(
                    "nib_reminder",
                    "Daily reminder",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
        }
    }
}
