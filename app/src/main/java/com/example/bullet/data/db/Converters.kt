package com.example.bullet.data.db

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromBulletType(value: BulletType): String = value.name

    @TypeConverter
    fun toBulletType(value: String): BulletType = BulletType.valueOf(value)

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus): String = value.name

    @TypeConverter
    fun toTaskStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromFrequency(value: Frequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)
}
