package com.companion.learning.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.dao.NoteDao
import com.companion.learning.data.local.entity.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class NotesUiState(
    val notes: List<NoteEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteDao: NoteDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigation args: topicId and roadmapId are optional
    private val topicId: String? = savedStateHandle["id"]
    private val roadmapId: String? = savedStateHandle["roadmapId"]
    private val todayDate: String = LocalDate.now().toString() // "YYYY-MM-DD"

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Show notes filtered by topicId if available, else today's date
            val flow = if (topicId != null) {
                noteDao.getNotesForTopic(topicId)
            } else {
                noteDao.getNotesForDate(todayDate)
            }
            flow.collectLatest { notes ->
                _uiState.value = NotesUiState(notes = notes, isLoading = false)
            }
        }
    }

    fun addNote(title: String, content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            noteDao.insertNote(
                NoteEntity(
                    id = UUID.randomUUID().toString(),
                    roadmapId = roadmapId ?: "",
                    curriculumItemId = topicId,
                    dayDate = todayDate,
                    title = title.ifBlank { "Note" },
                    content = content,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    fun deleteNote(id: String) {
        viewModelScope.launch {
            noteDao.deleteNote(id)
        }
    }

    fun updateNote(id: String, title: String, content: String) {
        viewModelScope.launch {
            noteDao.updateNote(id, title, content, System.currentTimeMillis())
        }
    }
}
