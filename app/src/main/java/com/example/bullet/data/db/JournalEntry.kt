package com.example.bullet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Feeling(val label: String, val emoji: String) {
    HAPPY("Happy", "😊"),
    SAD("Sad", "😢"),
    ANGRY("Angry", "😠"),
    FEARFUL("Fearful", "😨"),
    DISGUSTED("Disgusted", "🤢"),
    SURPRISED("Surprised", "😮"),
}

fun String.toFeelingSet(): Set<Feeling> =
    if (isBlank()) emptySet()
    else split(",").mapNotNull { name -> Feeling.entries.find { it.name == name } }.toSet()

fun Set<Feeling>.toFeelingString(): String = joinToString(",") { it.name }

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,      // ISO "2025-01-09"
    val content: String,
    val feelings: String = "",  // comma-separated Feeling names
    val createdAt: Long = System.currentTimeMillis(),
)
