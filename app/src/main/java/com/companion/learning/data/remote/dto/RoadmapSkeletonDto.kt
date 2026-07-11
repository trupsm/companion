package com.companion.learning.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RoadmapSkeletonDto(
    val title: String,
    val summary: String,
    val milestones: List<MilestoneDto>
)

@Serializable
data class MilestoneDto(
    val weekNumber: Int,
    val title: String,
    val summary: String,
    val days: List<DayDto> = emptyList()
)

@Serializable
data class DayDto(
    val dayNumber: Int,
    val topic: String,
    val description: String = ""
)

@Serializable
data class QuizQuestionDto(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

@Serializable
data class ResourceDto(
    val title: String,
    val type: String, // VIDEO, DOCS, PRACTICE, ARTICLE
    val url: String
)
