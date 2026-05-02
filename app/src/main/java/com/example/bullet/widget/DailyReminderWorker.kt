package com.example.bullet.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bullet.data.db.TaskStatus
import com.example.bullet.di.WidgetEntryPoint
import com.example.bullet.notifications.postDailyReminderNotification
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DailyReminderWorker(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = EntryPointAccessors
            .fromApplication(appContext, WidgetEntryPoint::class.java)
            .taskRepository()

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val openCount = repository
            .getTasksForDate(today)
            .first()
            .count { it.status == TaskStatus.OPEN || it.status == TaskStatus.PUSHED }

        postDailyReminderNotification(appContext, openCount)
        return Result.success()
    }
}
