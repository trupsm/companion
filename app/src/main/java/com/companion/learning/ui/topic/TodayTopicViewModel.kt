package com.companion.learning.ui.topic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.remote.dto.ResourceDto
import com.companion.learning.domain.provider.LlmProvider
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.domain.repository.QuizRepository
import com.companion.learning.domain.repository.RoadmapRepository
import com.companion.learning.domain.repository.StreakRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TopicUiState(
    val item: CurriculumItemEntity? = null,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val resources: List<ResourceDto> = emptyList(),
    val isLoadingResources: Boolean = false,
    val resourceError: String? = null,
    val hasQuiz: Boolean = false
)

@HiltViewModel
class TodayTopicViewModel @Inject constructor(
    private val curriculumRepository: CurriculumRepository,
    private val quizRepository: QuizRepository,
    private val llmProvider: LlmProvider,
    private val roadmapRepository: RoadmapRepository,
    private val streakRepository: StreakRepository,
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
                return@launch
            }
            // Auto-transition NOT_STARTED → IN_PROGRESS when topic is opened
            curriculumRepository.markTopicInProgress(topicId)
            val updatedItem = curriculumRepository.getCurriculumItemById(topicId) ?: item
            val hasQuiz = quizRepository.hasQuizForItem(topicId)
            _uiState.value = TopicUiState(item = updatedItem, isLoading = false, hasQuiz = hasQuiz)
        }
    }

    fun markTopicCompleted() {
        viewModelScope.launch {
            curriculumRepository.updateCurriculumItemStatus(topicId, "COMPLETED")
            streakRepository.recordStudySessionToday()
            val item = curriculumRepository.getCurriculumItemById(topicId)
            if (item != null) {
                _uiState.update { it.copy(item = item) }
            }
        }
    }

    fun loadResources(roadmapGoal: String) {
        val item = _uiState.value.item ?: return
        if (_uiState.value.resources.isNotEmpty() || _uiState.value.isLoadingResources) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingResources = true, resourceError = null) }
            llmProvider.recommendResources(topic = item.topic, goal = roadmapGoal)
                .onSuccess { resources ->
                    _uiState.update { it.copy(resources = resources, isLoadingResources = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingResources = false,
                            resourceError = error.message ?: "Failed to load resources"
                        )
                    }
                }
        }
    }
}
