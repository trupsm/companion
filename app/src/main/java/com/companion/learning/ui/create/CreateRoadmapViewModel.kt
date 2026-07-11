package com.companion.learning.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.MilestoneEntity
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.domain.provider.LlmProvider
import com.companion.learning.domain.repository.RoadmapRepository
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.data.local.entity.CurriculumItemEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed class GenerationState {
    object Idle : GenerationState()
    object Loading : GenerationState()
    object Success : GenerationState()
    data class Error(val message: String) : GenerationState()
}

@HiltViewModel
class CreateRoadmapViewModel @Inject constructor(
    private val llmProvider: LlmProvider,
    private val roadmapRepository: RoadmapRepository,
    private val curriculumRepository: CurriculumRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenerationState>(GenerationState.Idle)
    val uiState: StateFlow<GenerationState> = _uiState.asStateFlow()

    fun generateRoadmap(goal: String, duration: String, level: String, hoursPerDay: Int) {
        viewModelScope.launch {
            _uiState.value = GenerationState.Loading
            
            // 1. Fetch active roadmaps and sum their allocated hours
            val activeRoadmaps = roadmapRepository.getAllRoadmaps().first().filter { it.status == "ACTIVE" }
            val sumActiveHours = activeRoadmaps.sumOf { it.hoursPerDay }
            
            // 2. Fetch global study hours limit from settings
            val settingsHours = secureStorage.getStudyHours().toIntOrNull() ?: 2
            
            // 3. Perform Validation
            if (sumActiveHours + hoursPerDay > settingsHours) {
                _uiState.value = GenerationState.Error(
                    "Daily limit exceeded! Allocating $hoursPerDay hr/day for this course plus your existing active courses ($sumActiveHours hr/day) exceeds your global daily budget of $settingsHours hr/day in Settings."
                )
                return@launch
            }

            // 4. API Key Check: Fall back to customized mock generation if blank
            val apiKey = secureStorage.getApiKey()
            if (apiKey.isNullOrBlank()) {
                generateAndSaveMockRoadmap(goal, duration, level, hoursPerDay)
                return@launch
            }

            val result = llmProvider.generateSkeleton(goal, duration, level)
            
            result.onSuccess { dto ->
                val roadmapId = UUID.randomUUID().toString()
                
                val roadmap = RoadmapEntity(
                    id = roadmapId,
                    title = dto.title,
                    goal = goal,
                    duration = duration,
                    experienceLevel = level,
                    hoursPerDay = hoursPerDay,
                    llmProviderId = "GEMINI",
                    schemaVersion = 1,
                    status = "NOT_YET_STARTED",
                    startedAt = null,
                    pausedAt = null,
                    createdAt = System.currentTimeMillis()
                )
                
                val milestones = dto.milestones.map {
                    MilestoneEntity(
                        id = UUID.randomUUID().toString(),
                        roadmapId = roadmapId,
                        weekNumber = it.weekNumber,
                        title = it.title,
                        summary = it.summary,
                        expansionStatus = "PENDING"
                    )
                }
                
                roadmapRepository.createRoadmap(roadmap, milestones)
                _uiState.value = GenerationState.Success
            }.onFailure { error ->
                _uiState.value = GenerationState.Error(error.message ?: "Unknown error occurred")
            }
        }
    }

    private suspend fun generateAndSaveMockRoadmap(goal: String, duration: String, level: String, hoursPerDay: Int) {
        val roadmapId = UUID.randomUUID().toString()
        val title = if (goal.length > 25) goal.take(22) + "..." else goal
        
        val roadmap = RoadmapEntity(
            id = roadmapId,
            title = "Demo: $title",
            goal = goal,
            duration = duration,
            experienceLevel = level,
            hoursPerDay = hoursPerDay,
            llmProviderId = "MOCK_DEMO",
            schemaVersion = 1,
            status = "NOT_YET_STARTED",
            startedAt = null,
            pausedAt = null,
            createdAt = System.currentTimeMillis()
        )

        val milestones = listOf(
            MilestoneEntity(
                id = "demo-ms-1-" + UUID.randomUUID().toString().take(4),
                roadmapId = roadmapId,
                weekNumber = 1,
                title = "Foundations of $title",
                summary = "Introduction and core theoretical syntax required for mastering $goal.",
                expansionStatus = "EXPANDED"
            ),
            MilestoneEntity(
                id = "demo-ms-2-" + UUID.randomUUID().toString().take(4),
                roadmapId = roadmapId,
                weekNumber = 2,
                title = "Intermediate Concepts & Patterns",
                summary = "Diving into application flow, tools, and algorithms related to $goal.",
                expansionStatus = "PENDING"
            ),
            MilestoneEntity(
                id = "demo-ms-3-" + UUID.randomUUID().toString().take(4),
                roadmapId = roadmapId,
                weekNumber = 3,
                title = "Hands-on Exercises & Final Project",
                summary = "Create concrete deliverables and review error resolution strategies.",
                expansionStatus = "PENDING"
            )
        )

        val curriculumItems = listOf(
            CurriculumItemEntity(
                id = UUID.randomUUID().toString(),
                roadmapId = roadmapId,
                milestoneId = milestones[0].id,
                dayNumber = 1,
                topic = "Environment Setup & First Syntax",
                description = "Configure working IDE, check compiler, and print first logs for $goal.",
                estimatedTime = 60,
                status = "NOT_STARTED"
            ),
            CurriculumItemEntity(
                id = UUID.randomUUID().toString(),
                roadmapId = roadmapId,
                milestoneId = milestones[0].id,
                dayNumber = 2,
                topic = "Core Functions & Logic Blocks",
                description = "Declare functions, arguments, return properties, and simple validation checks.",
                estimatedTime = 90,
                status = "NOT_STARTED"
            ),
            CurriculumItemEntity(
                id = UUID.randomUUID().toString(),
                roadmapId = roadmapId,
                milestoneId = milestones[0].id,
                dayNumber = 3,
                topic = "Data Structures & Iterators",
                description = "Work with lists, maps, collections, and simple for/while loops.",
                estimatedTime = 120,
                status = "NOT_STARTED"
            )
        )

        roadmapRepository.createRoadmap(roadmap, milestones)
        curriculumRepository.insertCurriculumItems(curriculumItems)
        _uiState.value = GenerationState.Success
    }
}
