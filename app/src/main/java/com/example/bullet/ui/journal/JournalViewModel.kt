package com.example.bullet.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullet.data.db.JournalEntry
import com.example.bullet.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val entries: StateFlow<List<JournalEntry>> = repository
        .getAllJournalEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addEntry(content: String, date: String) {
        viewModelScope.launch {
            repository.insertJournalEntry(JournalEntry(date = date, content = content.trim()))
        }
    }

    fun updateEntry(entry: JournalEntry, newContent: String) {
        viewModelScope.launch {
            repository.updateJournalEntry(entry.copy(content = newContent.trim()))
        }
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            repository.deleteJournalEntry(entry)
        }
    }
}
