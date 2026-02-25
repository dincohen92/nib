package com.example.bullet.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullet.data.db.BulletType
import com.example.bullet.data.db.Task
import com.example.bullet.data.db.TaskStatus
import com.example.bullet.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

enum class ViewMode { DAY, WEEK, MONTH }

data class TaskCounts(val open: Int, val closed: Int)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val selectedDate = MutableStateFlow(LocalDate.now())
    val viewMode = MutableStateFlow(ViewMode.DAY)

    init {
        viewModelScope.launch {
            repository.migratePastOpenTasks()
            repository.generateRecurringTasksForToday()
        }
    }

    // Tasks for the currently selected day
    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForSelectedDate: StateFlow<List<Task>> = selectedDate
        .flatMapLatest { date ->
            repository.getTasksForDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Counts for every day in the visible range
    @OptIn(ExperimentalCoroutinesApi::class)
    val taskCountsByDate: StateFlow<Map<LocalDate, TaskCounts>> =
        combine(selectedDate, viewMode) { date, mode -> visibleRange(date, mode) }
            .flatMapLatest { (from, to) ->
                repository.getTasksForDateRange(
                    from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    to.format(DateTimeFormatter.ISO_LOCAL_DATE)
                ).map { tasks ->
                    tasks.groupBy { LocalDate.parse(it.date) }
                        .mapValues { (_, dayTasks) ->
                            TaskCounts(
                                open = dayTasks.count { it.status == TaskStatus.OPEN || it.status == TaskStatus.PUSHED },
                                closed = dayTasks.count { it.status == TaskStatus.CLOSED }
                            )
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )

    private fun visibleRange(date: LocalDate, mode: ViewMode): Pair<LocalDate, LocalDate> =
        when (mode) {
            ViewMode.DAY -> date to date
            ViewMode.WEEK -> {
                val mon = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                mon to mon.plusDays(6)
            }
            ViewMode.MONTH -> {
                val first = date.withDayOfMonth(1)
                first to first.with(TemporalAdjusters.lastDayOfMonth())
            }
        }

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun setViewMode(mode: ViewMode) {
        viewMode.value = mode
    }

    fun cycleViewMode(forward: Boolean) {
        val modes = ViewMode.entries
        val idx = modes.indexOf(viewMode.value)
        viewMode.value = modes[(idx + if (forward) 1 else -1).coerceIn(0, modes.lastIndex)]
    }

    fun navigatePeriod(forward: Boolean) {
        val delta = if (forward) 1L else -1L
        selectedDate.value = when (viewMode.value) {
            ViewMode.DAY -> selectedDate.value.plusDays(delta)
            ViewMode.WEEK -> selectedDate.value.plusWeeks(delta)
            ViewMode.MONTH -> selectedDate.value.plusMonths(delta)
        }
    }

    fun goToToday() {
        selectedDate.value = LocalDate.now()
    }

    fun addTask(content: String, date: LocalDate) {
        viewModelScope.launch {
            repository.insertTask(
                Task(
                    content = content,
                    bulletType = BulletType.TASK,
                    status = TaskStatus.OPEN,
                    date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                )
            )
        }
    }

    fun cycleStatus(task: Task) {
        val nextStatus = when (task.status) {
            TaskStatus.OPEN, TaskStatus.PUSHED -> TaskStatus.CLOSED
            TaskStatus.CLOSED -> TaskStatus.OPEN
        }
        viewModelScope.launch { repository.updateTask(task.copy(status = nextStatus)) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    fun editTaskContent(task: Task, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch { repository.updateTask(task.copy(content = content.trim())) }
    }

    fun saveTaskEdits(task: Task, content: String, date: LocalDate) {
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    content = content.trim(),
                    date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                )
            )
        }
    }
}
