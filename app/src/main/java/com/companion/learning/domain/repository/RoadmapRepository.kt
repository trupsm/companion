package com.companion.learning.domain.repository

import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

interface RoadmapRepository {
    suspend fun createRoadmap(roadmap: RoadmapEntity, milestones: List<MilestoneEntity>)
    suspend fun getRoadmap(id: String): RoadmapEntity?
    fun getAllRoadmaps(): Flow<List<RoadmapEntity>>
    fun getMilestonesForRoadmap(roadmapId: String): Flow<List<MilestoneEntity>>
    suspend fun deleteRoadmap(id: String)
    suspend fun updateRoadmap(id: String, title: String, goal: String)
    suspend fun startRoadmap(id: String, startedAt: Long)
    suspend fun pauseRoadmap(id: String)
    suspend fun resumeRoadmap(id: String)
    suspend fun updateMilestoneExpansionStatus(milestoneId: String, status: String)
}
