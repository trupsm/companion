package com.companion.learning.domain.provider

import com.companion.learning.data.remote.dto.RoadmapSkeletonDto

interface LlmProvider {
    suspend fun generateSkeleton(goal: String, duration: String, level: String): Result<RoadmapSkeletonDto>
    suspend fun standardizeRoadmap(rawText: String, title: String): Result<RoadmapSkeletonDto>
}
