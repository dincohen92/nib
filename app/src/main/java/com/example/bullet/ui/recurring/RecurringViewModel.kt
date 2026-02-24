package com.example.bullet.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullet.data.db.Frequency
import com.example.bullet.data.db.RecurringTask
import com.example.bullet.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecurringViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val rules: StateFlow<List<RecurringTask>> = repository
        .getAllRecurringTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun add(
        title: String,
        frequency: Frequency,
        dayOfWeek: Int? = null,
        dayOfMonth: Int? = null,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertRecurringTask(
                RecurringTask(
                    title = title.trim(),
                    frequency = frequency,
                    dayOfWeek = dayOfWeek,
                    dayOfMonth = dayOfMonth,
                )
            )
        }
    }

    fun save(
        rule: RecurringTask,
        title: String,
        frequency: Frequency,
        dayOfWeek: Int? = null,
        dayOfMonth: Int? = null,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.updateRecurringTask(
                rule.copy(
                    title = title.trim(),
                    frequency = frequency,
                    dayOfWeek = dayOfWeek,
                    dayOfMonth = dayOfMonth,
                )
            )
        }
    }

    fun toggleActive(rule: RecurringTask) {
        viewModelScope.launch {
            repository.updateRecurringTask(rule.copy(isActive = !rule.isActive))
        }
    }

    fun delete(rule: RecurringTask) {
        viewModelScope.launch { repository.deleteRecurringTask(rule) }
    }
}
