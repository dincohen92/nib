package com.example.bullet.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bullet.di.WidgetEntryPoint
import com.example.bullet.notifications.postJournalReminderNotification
import dagger.hilt.android.EntryPointAccessors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class JournalReminderWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = EntryPointAccessors
            .fromApplication(appContext, WidgetEntryPoint::class.java)
            .taskRepository()

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        if (!repository.hasJournalEntryForDate(today)) {
            postJournalReminderNotification(appContext)
        }
        return Result.success()
    }
}
