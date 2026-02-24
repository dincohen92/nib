package com.example.bullet.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AspirationDao {

    @Query("SELECT * FROM aspirations ORDER BY createdAt ASC")
    fun getAllAspirations(): Flow<List<Aspiration>>

    @Insert
    suspend fun insert(aspiration: Aspiration): Long

    @Delete
    suspend fun delete(aspiration: Aspiration)
}
