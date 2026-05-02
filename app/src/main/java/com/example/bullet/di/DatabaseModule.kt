package com.example.bullet.di

import android.content.Context
import androidx.room.Room
import com.example.bullet.data.db.AspirationDao
import com.example.bullet.data.db.JournalEntryDao
import com.example.bullet.data.db.MIGRATION_4_5
import com.example.bullet.data.db.MIGRATION_5_6
import com.example.bullet.data.db.NibDatabase
import com.example.bullet.data.db.RecurringTaskDao
import com.example.bullet.data.db.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NibDatabase =
        Room.databaseBuilder(
            context,
            NibDatabase::class.java,
            "bullet.db"
        )
            .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideTaskDao(db: NibDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideAspirationDao(db: NibDatabase): AspirationDao = db.aspirationDao()

    @Provides
    fun provideRecurringTaskDao(db: NibDatabase): RecurringTaskDao = db.recurringTaskDao()

    @Provides
    fun provideJournalEntryDao(db: NibDatabase): JournalEntryDao = db.journalEntryDao()
}
