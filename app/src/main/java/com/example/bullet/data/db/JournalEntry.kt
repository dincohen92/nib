package com.example.bullet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,      // ISO "2025-01-09"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
)
