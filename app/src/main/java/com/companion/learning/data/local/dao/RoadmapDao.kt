package com.companion.learning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.companion.learning.data.local.entity.RoadmapEntity
import com.companion.learning.data.local.entity.MilestoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoadmapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: RoadmapEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<MilestoneEntity>)

    @Query("SELECT * FROM roadmaps WHERE id = :id")
    suspend fun getRoadmapById(id: String): RoadmapEntity?
    
    @Query("SELECT * FROM roadmaps")
    fun getAllRoadmaps(): Flow<List<RoadmapEntity>>

    @Query("SELECT * FROM milestones WHERE roadmapId = :roadmapId ORDER BY weekNumber")
    fun getMilestonesForRoadmap(roadmapId: String): Flow<List<MilestoneEntity>>

    @Query("DELETE FROM roadmaps WHERE id = :id")
    suspend fun deleteRoadmapById(id: String)

    @Query("UPDATE roadmaps SET title = :title, goal = :goal WHERE id = :id")
    suspend fun updateRoadmapDetails(id: String, title: String, goal: String)

    @Query("UPDATE roadmaps SET status = :status, startedAt = :startedAt, pausedAt = :pausedAt WHERE id = :id")
    suspend fun updateRoadmapLifecycle(id: String, status: String, startedAt: Long?, pausedAt: Long?)

    @Query("UPDATE milestones SET expansionStatus = :status WHERE id = :id")
    suspend fun updateMilestoneStatus(id: String, status: String)
}
