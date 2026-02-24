package com.example.bullet.ui.aspirations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bullet.data.db.Aspiration
import com.example.bullet.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AspirationsViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val aspirations: StateFlow<List<Aspiration>> = repository
        .getAllAspirations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun addAspiration(title: String, note: String) {
        viewModelScope.launch {
            repository.insertAspiration(Aspiration(title = title, note = note.trim()))
        }
    }

    fun deleteAspiration(aspiration: Aspiration) {
        viewModelScope.launch {
            repository.deleteAspiration(aspiration)
        }
    }
}
