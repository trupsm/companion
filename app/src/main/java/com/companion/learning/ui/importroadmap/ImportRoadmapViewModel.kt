package com.companion.learning.ui.importroadmap

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.MilestoneEntity
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.data.parser.DocumentParser
import com.companion.learning.data.parser.RoadmapTextParser
import com.companion.learning.data.remote.dto.RoadmapSkeletonDto
import com.companion.learning.domain.repository.CurriculumRepository
import com.companion.learning.domain.repository.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

import com.companion.learning.domain.provider.LlmProvider

sealed class ImportState {
    object Idle : ImportState()
    object Parsing : ImportState()
    data class Preview(
        val dto: RoadmapSkeletonDto,
        val title: String,
        val hours: String,
        val duration: String,
        val level: String
    ) : ImportState()
    object Saving : ImportState()
    object Success : ImportState()
    data class Error(val message: String) : ImportState()
}

@HiltViewModel
class ImportRoadmapViewModel @Inject constructor(
    private val documentParser: DocumentParser,
    private val roadmapTextParser: RoadmapTextParser,
    private val roadmapRepository: RoadmapRepository,
    private val curriculumRepository: CurriculumRepository,
    private val secureStorage: SecureStorage,
    private val llmProvider: LlmProvider
) : ViewModel() {

    private val _state = MutableStateFlow<ImportState>(ImportState.Idle)
    val state: StateFlow<ImportState> = _state.asStateFlow()

    fun parseDocument(uri: Uri, title: String, hours: String, duration: String, level: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = ImportState.Parsing

            val activeRoadmaps = roadmapRepository.getAllRoadmaps().first().filter { it.status == "ACTIVE" }
            val sumActiveHours = activeRoadmaps.sumOf { it.hoursPerDay }
            val settingsHours = secureStorage.getStudyHours().toIntOrNull() ?: 2
            val courseHours = hours.toIntOrNull() ?: 2

            if (sumActiveHours + courseHours > settingsHours) {
                _state.value = ImportState.Error(
                    "Daily limit exceeded! Allocating $courseHours hr/day for this course plus your existing active courses ($sumActiveHours hr/day) exceeds your global daily budget of $settingsHours hr/day in Settings."
                )
                return@launch
            }

            val result = documentParser.extractText(uri)
            result.onSuccess { rawText ->
                val apiKey = secureStorage.getApiKey()
                if (!apiKey.isNullOrBlank()) {
                    val aiResult = llmProvider.standardizeRoadmap(rawText, title.ifBlank { "My Roadmap" })
                    aiResult.onSuccess { dto ->
                        _state.value = ImportState.Preview(dto, title, hours, duration, level)
                    }.onFailure { error ->
                        // Fallback to offline parser
                        val dto = roadmapTextParser.parse(rawText, title.ifBlank { "My Roadmap" })
                        _state.value = ImportState.Preview(dto, title, hours, duration, level)
                    }
                } else {
                    // Fallback to offline parser
                    val dto = roadmapTextParser.parse(rawText, title.ifBlank { "My Roadmap" })
                    _state.value = ImportState.Preview(dto, title, hours, duration, level)
                }
            }.onFailure { error ->
                _state.value = ImportState.Error(error.message ?: "Failed to parse document")
            }
        }
    }

    fun parseRawText(rawText: String, title: String, hours: String, duration: String, level: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (rawText.isBlank()) {
                _state.value = ImportState.Error("Please enter or paste roadmap text first.")
                return@launch
            }
            _state.value = ImportState.Parsing

            val activeRoadmaps = roadmapRepository.getAllRoadmaps().first().filter { it.status == "ACTIVE" }
            val sumActiveHours = activeRoadmaps.sumOf { it.hoursPerDay }
            val settingsHours = secureStorage.getStudyHours().toIntOrNull() ?: 2
            val courseHours = hours.toIntOrNull() ?: 2

            if (sumActiveHours + courseHours > settingsHours) {
                _state.value = ImportState.Error(
                    "Daily limit exceeded! Allocating $courseHours hr/day for this course plus your existing active courses ($sumActiveHours hr/day) exceeds your global daily budget of $settingsHours hr/day in Settings."
                )
                return@launch
            }

            val apiKey = secureStorage.getApiKey()
            if (!apiKey.isNullOrBlank()) {
                val aiResult = llmProvider.standardizeRoadmap(rawText, title.ifBlank { "My Roadmap" })
                aiResult.onSuccess { dto ->
                    _state.value = ImportState.Preview(dto, title, hours, duration, level)
                }.onFailure { error ->
                    // Fallback to offline parser
                    val dto = roadmapTextParser.parse(rawText, title.ifBlank { "My Roadmap" })
                    _state.value = ImportState.Preview(dto, title, hours, duration, level)
                }
            } else {
                // Fallback to offline parser
                val dto = roadmapTextParser.parse(rawText, title.ifBlank { "My Roadmap" })
                _state.value = ImportState.Preview(dto, title, hours, duration, level)
            }
        }
    }

    fun saveRoadmap(dto: RoadmapSkeletonDto, goal: String, hours: String, duration: String, level: String) {
        viewModelScope.launch {
            _state.value = ImportState.Saving

            val courseHours = hours.toIntOrNull() ?: 2
            val activeRoadmaps = roadmapRepository.getAllRoadmaps().first().filter { it.status == "ACTIVE" }
            val sumActiveHours = activeRoadmaps.sumOf { it.hoursPerDay }
            val settingsHours = secureStorage.getStudyHours().toIntOrNull() ?: 2

            if (sumActiveHours + courseHours > settingsHours) {
                _state.value = ImportState.Error(
                    "Daily limit exceeded! Allocating $courseHours hr/day for this course plus your existing active courses ($sumActiveHours hr/day) exceeds your global daily budget of $settingsHours hr/day in Settings."
                )
                return@launch
            }

            val roadmapId = UUID.randomUUID().toString()
            val roadmap = RoadmapEntity(
                id = roadmapId,
                title = dto.title,
                goal = goal,
                duration = duration,
                experienceLevel = level,
                hoursPerDay = courseHours,
                llmProviderId = "MANUAL_IMPORT",
                schemaVersion = 1,
                status = "NOT_YET_STARTED",
                startedAt = null,
                pausedAt = null,
                createdAt = System.currentTimeMillis()
            )

            // Build milestones and curriculum items
            val milestones = mutableListOf<MilestoneEntity>()
            val curriculumItems = mutableListOf<CurriculumItemEntity>()

            for (milestoneDto in dto.milestones) {
                val milestoneId = UUID.randomUUID().toString()
                milestones.add(
                    MilestoneEntity(
                        id = milestoneId,
                        roadmapId = roadmapId,
                        weekNumber = milestoneDto.weekNumber,
                        title = milestoneDto.title,
                        summary = milestoneDto.summary,
                        expansionStatus = "EXPANDED"
                    )
                )

                // Create CurriculumItemEntity for each parsed day in this milestone
                for (dayDto in milestoneDto.days) {
                    curriculumItems.add(
                        CurriculumItemEntity(
                            id = UUID.randomUUID().toString(),
                            roadmapId = roadmapId,
                            milestoneId = milestoneId,
                            dayNumber = dayDto.dayNumber,
                            topic = dayDto.topic,
                            description = dayDto.description.ifBlank { null },
                            estimatedTime = courseHours * 60,
                            status = "NOT_STARTED"
                        )
                    )
                }
            }

            roadmapRepository.createRoadmap(roadmap, milestones)
            if (curriculumItems.isNotEmpty()) {
                curriculumRepository.insertCurriculumItems(curriculumItems)
            }

            _state.value = ImportState.Success
        }
    }

    fun reset() {
        _state.value = ImportState.Idle
    }
}
