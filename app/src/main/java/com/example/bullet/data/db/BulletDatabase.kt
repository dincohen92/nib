package com.example.bullet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Task::class, Aspiration::class, RecurringTask::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NibDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun aspirationDao(): AspirationDao
    abstract fun recurringTaskDao(): RecurringTaskDao
}
