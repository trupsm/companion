package com.companion.learning.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.MilestoneEntity
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.domain.repository.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.companion.learning.domain.provider.LlmProvider
import com.companion.learning.data.local.security.SecureStorage
import java.util.UUID

data class RoadmapDetailsUiState(
    val roadmap: RoadmapEntity? = null,
    val milestones: List<MilestoneEntity> = emptyList(),
    val curriculumItems: List<CurriculumItemEntity> = emptyList(),
    val isExpanding: Boolean = false,
    val expansionError: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class RoadmapDetailsViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val curriculumRepository: CurriculumRepository,
    private val llmProvider: LlmProvider,
    private val secureStorage: SecureStorage,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roadmapId: String = checkNotNull(savedStateHandle["id"])

    private val _uiState = MutableStateFlow(RoadmapDetailsUiState())
    val uiState: StateFlow<RoadmapDetailsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val roadmap = roadmapRepository.getRoadmap(roadmapId)
            _uiState.update { it.copy(roadmap = roadmap) }
            
            // Collect Milestones
            launch {
                roadmapRepository.getMilestonesForRoadmap(roadmapId)
                    .collectLatest { milestones ->
                        _uiState.update { 
                            it.copy(milestones = milestones) 
                        }
                    }
            }

            // Collect Curriculum Items
            launch {
                curriculumRepository.getCurriculumItemsForRoadmap(roadmapId)
                    .collectLatest { items ->
                        _uiState.update { 
                            it.copy(curriculumItems = items, isLoading = false) 
                        }
                    }
            }
        }
    }

    fun startRoadmap() {
        viewModelScope.launch {
            roadmapRepository.startRoadmap(roadmapId, System.currentTimeMillis())
            // Reload metadata
            val updated = roadmapRepository.getRoadmap(roadmapId)
            _uiState.update { it.copy(roadmap = updated) }
        }
    }

    fun pauseRoadmap() {
        viewModelScope.launch {
            roadmapRepository.pauseRoadmap(roadmapId)
            val updated = roadmapRepository.getRoadmap(roadmapId)
            _uiState.update { it.copy(roadmap = updated) }
        }
    }

    fun resumeRoadmap() {
        viewModelScope.launch {
            roadmapRepository.resumeRoadmap(roadmapId)
            val updated = roadmapRepository.getRoadmap(roadmapId)
            _uiState.update { it.copy(roadmap = updated) }
        }
    }

    fun deleteRoadmap(onSuccess: () -> Unit) {
        viewModelScope.launch {
            roadmapRepository.deleteRoadmap(roadmapId)
            onSuccess()
        }
    }

    fun updateRoadmap(title: String, goal: String) {
        viewModelScope.launch {
            roadmapRepository.updateRoadmap(roadmapId, title, goal)
            // Reload metadata
            val updated = roadmapRepository.getRoadmap(roadmapId)
            _uiState.update { it.copy(roadmap = updated) }
        }
    }

    fun checkAndExpandMilestone(milestoneIndex: Int) {
        val uiState = _uiState.value
        val milestones = uiState.milestones
        if (milestoneIndex >= 0 && milestoneIndex < milestones.size) {
            val milestone = milestones[milestoneIndex]
            if (milestone.expansionStatus == "PENDING" && !uiState.isExpanding) {
                viewModelScope.launch {
                    expandMilestone(milestone)
                }
            }
        }
    }

    private suspend fun expandMilestone(milestone: MilestoneEntity) {
        val roadmap = _uiState.value.roadmap ?: return
        
        _uiState.update { it.copy(isExpanding = true, expansionError = null) }
        roadmapRepository.updateMilestoneExpansionStatus(milestone.id, "EXPANDING")

        val apiKey = secureStorage.getApiKey()
        if (apiKey.isNullOrBlank()) {
            // Generate mock fallback immediately
            val mockItems = generateMockDays(milestone, roadmap)
            curriculumRepository.insertCurriculumItems(mockItems)
            roadmapRepository.updateMilestoneExpansionStatus(milestone.id, "EXPANDED")
            _uiState.update { it.copy(isExpanding = false) }
            return
        }

        val result = llmProvider.expandMilestone(
            roadmapGoal = roadmap.goal,
            milestoneTitle = milestone.title,
            milestoneSummary = milestone.summary,
            weekNumber = milestone.weekNumber,
            hoursPerDay = roadmap.hoursPerDay
        )

        result.onSuccess { days ->
            val items = days.map { dayDto ->
                CurriculumItemEntity(
                    id = UUID.randomUUID().toString(),
                    roadmapId = roadmap.id,
                    milestoneId = milestone.id,
                    dayNumber = dayDto.dayNumber,
                    topic = dayDto.topic,
                    description = dayDto.description.ifBlank { null },
                    estimatedTime = roadmap.hoursPerDay * 60,
                    status = "NOT_STARTED"
                )
            }
            curriculumRepository.insertCurriculumItems(items)
            roadmapRepository.updateMilestoneExpansionStatus(milestone.id, "EXPANDED")
            _uiState.update { it.copy(isExpanding = false) }
        }.onFailure { error ->
            // Revert status to PENDING on failure
            roadmapRepository.updateMilestoneExpansionStatus(milestone.id, "PENDING")
            _uiState.update { 
                it.copy(
                    isExpanding = false, 
                    expansionError = "Failed to expand week: ${error.message ?: "Unknown error"}. Swipe or pull tab to retry."
                ) 
            }
        }
    }

    private fun generateMockDays(milestone: MilestoneEntity, roadmap: RoadmapEntity): List<CurriculumItemEntity> {
        val courseHours = roadmap.hoursPerDay
        val weekNumber = milestone.weekNumber
        return (1..7).map { index ->
            val dayNumber = (weekNumber - 1) * 7 + index
            CurriculumItemEntity(
                id = UUID.randomUUID().toString(),
                roadmapId = roadmap.id,
                milestoneId = milestone.id,
                dayNumber = dayNumber,
                topic = "Day $dayNumber: Intermediate topic on ${milestone.title.take(15)}",
                description = "Build solid hands-on project implementations and run basic tests for Day $dayNumber.",
                estimatedTime = courseHours * 60,
                status = "NOT_STARTED"
            )
        }
    }
}
