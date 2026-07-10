package com.companion.learning.ui.topic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.domain.repository.CurriculumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicUiState(
    val item: CurriculumItemEntity? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false
)

@HiltViewModel
class TodayTopicViewModel @Inject constructor(
    private val curriculumRepository: CurriculumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val topicId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(TopicUiState())
    val uiState: StateFlow<TopicUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val item = curriculumRepository.getCurriculumItemById(topicId)
            if (item == null) {
                _uiState.value = TopicUiState(isLoading = false, notFound = true)
            } else {
                _uiState.value = TopicUiState(item = item, isLoading = false)
            }
        }
    }

    fun markTopicCompleted() {
        viewModelScope.launch {
            curriculumRepository.updateCurriculumItemStatus(topicId, "COMPLETED")
            val item = curriculumRepository.getCurriculumItemById(topicId)
            if (item != null) {
                _uiState.value = TopicUiState(item = item, isLoading = false)
            }
        }
    }
}
