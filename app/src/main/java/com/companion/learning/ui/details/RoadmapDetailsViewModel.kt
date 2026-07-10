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

data class RoadmapDetailsUiState(
    val roadmap: RoadmapEntity? = null,
    val milestones: List<MilestoneEntity> = emptyList(),
    val curriculumItems: List<CurriculumItemEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class RoadmapDetailsViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val curriculumRepository: CurriculumRepository,
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
}
