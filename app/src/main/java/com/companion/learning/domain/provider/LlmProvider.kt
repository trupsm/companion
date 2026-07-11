package com.companion.learning.domain.provider

import com.companion.learning.data.remote.dto.RoadmapSkeletonDto
import com.companion.learning.data.remote.dto.DayDto
import com.companion.learning.data.remote.dto.QuizQuestionDto
import com.companion.learning.data.remote.dto.ResourceDto

interface LlmProvider {
    suspend fun generateSkeleton(goal: String, duration: String, level: String): Result<RoadmapSkeletonDto>
    suspend fun standardizeRoadmap(rawText: String, title: String): Result<RoadmapSkeletonDto>
    suspend fun expandMilestone(
        roadmapGoal: String,
        milestoneTitle: String,
        milestoneSummary: String,
        weekNumber: Int,
        hoursPerDay: Int
    ): Result<List<DayDto>>
    suspend fun generateQuiz(topic: String, description: String, goal: String): Result<List<QuizQuestionDto>>
    suspend fun recommendResources(topic: String, goal: String): Result<List<ResourceDto>>
}
