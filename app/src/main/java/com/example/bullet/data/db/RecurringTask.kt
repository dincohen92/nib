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
    /** Day of month 1-31. Non-null only for MONTHLY "On day" mode. */
    val dayOfMonth: Int? = null,
    /** Ordinal: 1–4 or -1 (last). Non-null only for MONTHLY "On the" mode. */
    val monthlyOrdinal: Int? = null,
    /** ISO day-of-week 1=Mon…7=Sun. Non-null only for MONTHLY "On the" mode. */
    val monthlyWeekDay: Int? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)

fun RecurringTask.shouldFireOn(date: java.time.LocalDate): Boolean = when (frequency) {
    Frequency.DAILY -> true
    Frequency.WEEKLY -> dayOfWeek != null && date.dayOfWeek.value == dayOfWeek
    Frequency.MONTHLY -> when {
        monthlyOrdinal != null && monthlyWeekDay != null -> {
            val target = java.time.DayOfWeek.of(monthlyWeekDay)
            if (monthlyOrdinal == -1) {
                val last = date.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
                               .with(java.time.temporal.TemporalAdjusters.previousOrSame(target))
                date == last
            } else {
                val first = date.withDayOfMonth(1)
                    .with(java.time.temporal.TemporalAdjusters.nextOrSame(target))
                val nth = first.plusWeeks((monthlyOrdinal - 1).toLong())
                date == nth && nth.month == date.month
            }
        }
        dayOfMonth != null -> date.dayOfMonth == dayOfMonth &&
            dayOfMonth <= date.month.length(date.isLeapYear)
        else -> false
    }
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
    Frequency.MONTHLY -> when {
        monthlyOrdinal != null && monthlyWeekDay != null -> {
            val ordStr = if (monthlyOrdinal == -1) "last" else ordinal(monthlyOrdinal)
            val dayName = java.time.DayOfWeek.of(monthlyWeekDay)
                .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
            "Monthly on the $ordStr $dayName"
        }
        dayOfMonth != null -> "Monthly on the ${ordinal(dayOfMonth)}"
        else -> "Monthly"
    }
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
