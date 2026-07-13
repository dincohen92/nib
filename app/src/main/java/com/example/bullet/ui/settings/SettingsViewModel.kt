package com.example.bullet.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.bullet.data.repository.SettingsRepository
import com.example.bullet.widget.DailyReminderWorker
import com.example.bullet.widget.JournalReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
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

    private val _journalReminder = MutableStateFlow(settingsRepository.journalReminder)
    val journalReminder: StateFlow<Boolean> = _journalReminder

    fun setPushedTasksNotification(enabled: Boolean) {
        settingsRepository.pushedTasksNotification = enabled
        _pushedTasksNotification.value = enabled
    }

    fun setDailyReminder(enabled: Boolean) {
        settingsRepository.dailyReminder = enabled
        _dailyReminder.value = enabled

        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntilNextEightAm(), TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request,
            )
        } else {
            workManager.cancelUniqueWork("daily_reminder")
        }
    }

    fun setJournalReminder(enabled: Boolean) {
        settingsRepository.journalReminder = enabled
        _journalReminder.value = enabled

        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val request = PeriodicWorkRequestBuilder<JournalReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(millisUntilNextSixPm(), TimeUnit.MILLISECONDS)
                .build()
            workManager.enqueueUniquePeriodicWork(
                "journal_reminder",
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                request,
            )
        } else {
            workManager.cancelUniqueWork("journal_reminder")
        }
    }

    private fun millisUntilNextEightAm(): Long {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(8, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return ChronoUnit.MILLIS.between(now, next)
    }

    private fun millisUntilNextSixPm(): Long {
        val now = LocalDateTime.now()
        var next = now.toLocalDate().atTime(18, 0)
        if (!next.isAfter(now)) next = next.plusDays(1)
        return ChronoUnit.MILLIS.between(now, next)
    }
}
