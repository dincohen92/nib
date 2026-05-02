package com.example.bullet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC, createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date = :date ORDER BY createdAt DESC")
    fun getEntriesForDate(date: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE date >= :from AND date <= :to ORDER BY date ASC")
    fun getEntriesForDateRange(from: String, to: String): Flow<List<JournalEntry>>

    @Insert
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}
