package com.companion.learning.data.repository

import com.companion.learning.data.local.dao.RoadmapDao
import com.companion.learning.data.local.entity.MilestoneEntity
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.domain.repository.RoadmapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoadmapRepositoryImpl @Inject constructor(
    private val roadmapDao: RoadmapDao
) : RoadmapRepository {
    
    override suspend fun createRoadmap(roadmap: RoadmapEntity, milestones: List<MilestoneEntity>) {
        roadmapDao.insertRoadmap(roadmap)
        roadmapDao.insertMilestones(milestones)
    }

    override suspend fun getRoadmap(id: String): RoadmapEntity? {
        return roadmapDao.getRoadmapById(id)
    }

    override fun getAllRoadmaps(): Flow<List<RoadmapEntity>> {
        return roadmapDao.getAllRoadmaps()
    }

    override fun getMilestonesForRoadmap(roadmapId: String): Flow<List<MilestoneEntity>> {
        return roadmapDao.getMilestonesForRoadmap(roadmapId)
    }

    override suspend fun deleteRoadmap(id: String) {
        roadmapDao.deleteRoadmapById(id)
    }

    override suspend fun updateRoadmap(id: String, title: String, goal: String) {
        roadmapDao.updateRoadmapDetails(id, title, goal)
    }

    override suspend fun startRoadmap(id: String, startedAt: Long) {
        roadmapDao.updateRoadmapLifecycle(id, "ACTIVE", startedAt, null)
    }

    override suspend fun pauseRoadmap(id: String) {
        val roadmap = roadmapDao.getRoadmapById(id) ?: return
        if (roadmap.status == "ACTIVE") {
            val now = System.currentTimeMillis()
            roadmapDao.updateRoadmapLifecycle(
                id = id,
                status = "PAUSED",
                startedAt = roadmap.startedAt,
                pausedAt = now
            )
        }
    }

    override suspend fun resumeRoadmap(id: String) {
        val roadmap = roadmapDao.getRoadmapById(id) ?: return
        if (roadmap.status == "PAUSED" && roadmap.pausedAt != null && roadmap.startedAt != null) {
            val now = System.currentTimeMillis()
            val pauseDurationMs = now - roadmap.pausedAt
            val newStartedAt = roadmap.startedAt + pauseDurationMs
            roadmapDao.updateRoadmapLifecycle(
                id = id,
                status = "ACTIVE",
                startedAt = newStartedAt,
                pausedAt = null
            )
        }
    }

    override suspend fun updateMilestoneExpansionStatus(milestoneId: String, status: String) {
        roadmapDao.updateMilestoneStatus(milestoneId, status)
    }
}
