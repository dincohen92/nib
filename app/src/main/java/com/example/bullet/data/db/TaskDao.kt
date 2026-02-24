package com.example.bullet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY createdAt ASC")
    fun getTasksForDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date >= :from AND date <= :to ORDER BY date ASC, createdAt ASC")
    fun getTasksForDateRange(from: String, to: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date > :date ORDER BY date ASC, createdAt ASC")
    fun getTasksAfterDate(date: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY date DESC, createdAt ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE date = :date AND recurringTaskId = :recurringId LIMIT 1")
    suspend fun getTaskForDateAndRecurringId(date: String, recurringId: Long): Task?

    /** Migrate all incomplete (OPEN or PUSHED) tasks from before [toDate] to [toDate] as PUSHED. */
    @Query("UPDATE tasks SET date = :toDate, status = 'PUSHED' WHERE status IN ('OPEN', 'PUSHED') AND date < :toDate")
    suspend fun migrateOpenTasksToDate(toDate: String)
}
