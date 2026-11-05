package com.example.apkmoblineprojekt.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apkmoblineprojekt.data.NoteRepository
import com.example.apkmoblineprojekt.data.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NotesFilter { ALL, PINNED, LINKED }

data class NotesUiState(
    val query: String = "",
    val filter: NotesFilter = NotesFilter.ALL,
    val notes: List<Note> = emptyList(),
)

class NotesViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(NotesFilter.ALL)

    val uiState: StateFlow<NotesUiState> =
        combine(noteRepository.observeNotes(), query, filter) { notes, query, filter ->
            NotesUiState(
                query = query,
                filter = filter,
                notes = notes
                    .filter {
                        when (filter) {
                            NotesFilter.ALL -> true
                            NotesFilter.PINNED -> it.pinned
                            NotesFilter.LINKED -> it.taskId != null
                        }
                    }
                    .filter {
                        query.isBlank() ||
                            it.title.contains(query.trim(), ignoreCase = true) ||
                            it.content.contains(query.trim(), ignoreCase = true)
                    }
                    .sortedWith(compareByDescending<Note> { it.pinned }.thenByDescending { it.updatedAt }),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotesUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onFilterChange(value: NotesFilter) {
        filter.value = value
    }

    fun togglePinned(note: Note) {
        viewModelScope.launch { noteRepository.setPinned(note.id, !note.pinned) }
    }
}
