package com.companion.learning.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.QuizQuestionEntity
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.domain.repository.QuizRepository
import com.companion.learning.domain.repository.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

data class QuizUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val questions: List<QuizQuestionEntity> = emptyList(),
    val parsedOptions: List<List<String>> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,      // answer selected on current question
    val answeredCorrectly: List<Boolean?> = emptyList(), // per question result, null = not yet answered
    val isComplete: Boolean = false,
    val topicName: String = ""
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val curriculumRepository: CurriculumRepository,
    private val roadmapRepository: RoadmapRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val topicId: String = checkNotNull(savedStateHandle["id"])
    private val json = Json { ignoreUnknownKeys = true }

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuiz()
    }

    private fun loadQuiz() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val item = curriculumRepository.getCurriculumItemById(topicId)
            val topicName = item?.topic ?: "Topic"

            // Find roadmap goal for context (best-effort)
            val roadmapGoal = item?.let {
                roadmapRepository.getRoadmap(it.roadmapId)?.goal
            } ?: ""

            val hasExisting = quizRepository.hasQuizForItem(topicId)
            val questions = if (hasExisting) {
                quizRepository.getQuizForItem(topicId)
            } else {
                if (item == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Topic not found.") }
                    return@launch
                }
                val result = quizRepository.generateAndSaveQuiz(item, roadmapGoal)
                if (result.isFailure) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to generate quiz."
                        )
                    }
                    return@launch
                }
                result.getOrElse { emptyList() }
            }

            val parsedOptions = questions.map { q ->
                try { json.decodeFromString<List<String>>(q.options) }
                catch (e: Exception) { q.options.split(",").map { it.trim() } }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    questions = questions,
                    parsedOptions = parsedOptions,
                    answeredCorrectly = List(questions.size) { null },
                    topicName = topicName
                )
            }
        }
    }

    fun selectAnswer(answer: String) {
        // Only allow selection if not already answered this question
        if (_uiState.value.selectedAnswer != null) return
        _uiState.update { it.copy(selectedAnswer = answer) }
    }

    fun confirmAnswer() {
        val state = _uiState.value
        val selected = state.selectedAnswer ?: return
        val correct = state.questions.getOrNull(state.currentIndex)?.correctAnswer
        val isCorrect = selected == correct

        val newAnswered = state.answeredCorrectly.toMutableList()
        newAnswered[state.currentIndex] = isCorrect

        _uiState.update { it.copy(answeredCorrectly = newAnswered) }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex >= state.questions.size) {
            _uiState.update { it.copy(isComplete = true, selectedAnswer = null) }
        } else {
            _uiState.update { it.copy(currentIndex = nextIndex, selectedAnswer = null) }
        }
    }

    fun retake() {
        _uiState.update {
            it.copy(
                currentIndex = 0,
                selectedAnswer = null,
                answeredCorrectly = List(it.questions.size) { null },
                isComplete = false
            )
        }
    }

    fun retry() {
        loadQuiz()
    }
}
