package com.companion.learning.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.domain.repository.RoadmapRepository
import com.companion.learning.domain.repository.CurriculumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.companion.learning.data.mock.SeedData

data class RoadmapWithProgress(
    val roadmap: RoadmapEntity,
    val completed: Int = 0,
    val total: Int = 0
) {
    val progress: Float get() = if (total > 0) completed.toFloat() / total else 0f
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val curriculumRepository: CurriculumRepository
) : ViewModel() {

    private val _roadmaps = MutableStateFlow<List<RoadmapWithProgress>>(emptyList())
    val roadmaps: StateFlow<List<RoadmapWithProgress>> = _roadmaps.asStateFlow()

    init {
        viewModelScope.launch {
            val currentRoadmaps = roadmapRepository.getAllRoadmaps().first()
            if (currentRoadmaps.isEmpty()) {
                roadmapRepository.createRoadmap(SeedData.mockRoadmap, SeedData.mockMilestones)
                curriculumRepository.insertCurriculumItems(SeedData.mockCurriculumItems)
            }

            roadmapRepository.getAllRoadmaps().collectLatest { roadmaps ->
                val result = roadmaps.map { roadmap ->
                    val items = curriculumRepository.getCurriculumItemsForRoadmap(roadmap.id).first()
                    RoadmapWithProgress(
                        roadmap = roadmap,
                        completed = items.count { it.status == "COMPLETED" },
                        total = items.size
                    )
                }
                _roadmaps.value = result
            }
        }
    }
}
