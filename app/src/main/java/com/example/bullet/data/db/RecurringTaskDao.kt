package com.example.bullet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTaskDao {

    @Query("SELECT * FROM recurring_tasks ORDER BY createdAt ASC")
    fun getAll(): Flow<List<RecurringTask>>

    @Query("SELECT * FROM recurring_tasks WHERE isActive = 1")
    suspend fun getAllActive(): List<RecurringTask>

    @Insert
    suspend fun insert(task: RecurringTask): Long

    @Update
    suspend fun update(task: RecurringTask)

    @Delete
    suspend fun delete(task: RecurringTask)
}
