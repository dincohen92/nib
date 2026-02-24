package com.example.bullet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val bulletType: BulletType,
    val status: TaskStatus,
    /** ISO date string: yyyy-MM-dd */
    val date: String,
    val createdAt: Long = System.currentTimeMillis(),
    /** Non-null when this task was auto-generated from a RecurringTask rule. */
    val recurringTaskId: Long? = null,
)
