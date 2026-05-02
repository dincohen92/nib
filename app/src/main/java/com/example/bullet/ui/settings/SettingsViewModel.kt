package com.example.bullet.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bullet.data.repository.SettingsRepository
import com.example.bullet.widget.DailyReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _pushedTasksNotification = MutableStateFlow(settingsRepository.pushedTasksNotification)
    val pushedTasksNotification: StateFlow<Boolean> = _pushedTasksNotification

    private val _dailyReminder = MutableStateFlow(settingsRepository.dailyReminder)
    val dailyReminder: StateFlow<Boolean> = _dailyReminder

    fun setPushedTasksNotification(enabled: Boolean) {
        settingsRepository.pushedTasksNotification = enabled
        _pushedTasksNotification.value = enabled
    }

    fun setDailyReminder(enabled: Boolean) {
        settingsRepository.dailyReminder = enabled
        _dailyReminder.value = enabled

        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS).build()
            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        } else {
            workManager.cancelUniqueWork("daily_reminder")
        }
    }
}
