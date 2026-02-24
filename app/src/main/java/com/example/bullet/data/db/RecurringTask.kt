package com.example.bullet.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_tasks")
data class RecurringTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val frequency: Frequency,
    /** ISO day-of-week 1=Mon … 7=Sun. Non-null only for WEEKLY. */
    val dayOfWeek: Int? = null,
    /** Day of month 1-28. Non-null only for MONTHLY. */
    val dayOfMonth: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

fun RecurringTask.shouldFireOn(date: java.time.LocalDate): Boolean = when (frequency) {
    Frequency.DAILY -> true
    Frequency.WEEKLY -> dayOfWeek != null && date.dayOfWeek.value == dayOfWeek
    Frequency.MONTHLY -> dayOfMonth != null && date.dayOfMonth == dayOfMonth
}

fun RecurringTask.scheduleDescription(): String = when (frequency) {
    Frequency.DAILY -> "Daily"
    Frequency.WEEKLY -> {
        val day = dayOfWeek?.let {
            java.time.DayOfWeek.of(it)
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
        } ?: "?"
        "Weekly on $day"
    }
    Frequency.MONTHLY -> "Monthly on the ${ordinal(dayOfMonth ?: 1)}"
}

private fun ordinal(n: Int): String {
    val suffix = when {
        n in 11..13 -> "th"
        n % 10 == 1 -> "st"
        n % 10 == 2 -> "nd"
        n % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}
