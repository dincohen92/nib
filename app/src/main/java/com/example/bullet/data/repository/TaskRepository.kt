package com.example.bullet.data.repository

import android.content.Context
import com.example.bullet.data.db.Aspiration
import com.example.bullet.data.db.AspirationDao
import com.example.bullet.data.db.BulletType
import com.example.bullet.data.db.RecurringTask
import com.example.bullet.data.db.RecurringTaskDao
import com.example.bullet.data.db.Task
import com.example.bullet.data.db.TaskDao
import com.example.bullet.data.db.TaskStatus
import com.example.bullet.data.db.shouldFireOn
import com.example.bullet.widget.NibWidget
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val aspirationDao: AspirationDao,
    private val recurringTaskDao: RecurringTaskDao,
    @ApplicationContext private val context: Context
) {

    // ── Tasks ────────────────────────────────────────────────────────────────

    fun getAllTasks(): Flow<List<Task>> = taskDao.getAllTasks()

    fun getTasksForDate(date: String): Flow<List<Task>> = taskDao.getTasksForDate(date)

    fun getTasksForDateRange(from: String, to: String): Flow<List<Task>> =
        taskDao.getTasksForDateRange(from, to)

    suspend fun insertTask(task: Task): Long {
        val id = taskDao.insert(task)
        notifyWidget()
        return id
    }

    suspend fun updateTask(task: Task) {
        taskDao.update(task)
        notifyWidget()
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
        notifyWidget()
    }

    // ── Aspirations ──────────────────────────────────────────────────────────

    fun getAllAspirations(): Flow<List<Aspiration>> = aspirationDao.getAllAspirations()

    suspend fun insertAspiration(aspiration: Aspiration): Long = aspirationDao.insert(aspiration)

    suspend fun deleteAspiration(aspiration: Aspiration) = aspirationDao.delete(aspiration)

    // ── Recurring tasks ───────────────────────────────────────────────────────

    fun getAllRecurringTasks(): Flow<List<RecurringTask>> = recurringTaskDao.getAll()

    suspend fun insertRecurringTask(task: RecurringTask): Long = recurringTaskDao.insert(task)

    suspend fun updateRecurringTask(task: RecurringTask) = recurringTaskDao.update(task)

    suspend fun deleteRecurringTask(task: RecurringTask) = recurringTaskDao.delete(task)

    // ── Migration + generation ────────────────────────────────────────────────

    suspend fun migratePastOpenTasks() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        taskDao.migrateOpenTasksToDate(today)
        notifyWidget()
    }

    /**
     * For each active recurring rule that fires today, create a Task instance
     * unless one already exists for today (e.g. it was pushed here from yesterday).
     * Must be called AFTER [migratePastOpenTasks] so pushed instances are already on today.
     */
    suspend fun generateRecurringTasksForToday() {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val rules = recurringTaskDao.getAllActive()
        var generated = false
        for (rule in rules) {
            if (!rule.shouldFireOn(today)) continue
            if (taskDao.getTaskForDateAndRecurringId(todayStr, rule.id) != null) continue
            taskDao.insert(
                Task(
                    content = rule.title,
                    bulletType = BulletType.TASK,
                    status = TaskStatus.OPEN,
                    date = todayStr,
                    recurringTaskId = rule.id,
                )
            )
            generated = true
        }
        if (generated) notifyWidget()
    }

    // ── Widget refresh ───────────────────────────────────────────────────────

    private suspend fun notifyWidget() {
        NibWidget().updateAll(context)
    }
}
