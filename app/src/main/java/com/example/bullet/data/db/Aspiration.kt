package com.example.bullet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aspirations")
data class Aspiration(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
