package com.companion.learning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.companion.learning.data.local.entity.CurriculumItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurriculumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurriculumItems(items: List<CurriculumItemEntity>)

    @Query("SELECT * FROM curriculum_items WHERE dayNumber = :dayNumber")
    fun getCurriculumItemsForDay(dayNumber: Int): Flow<List<CurriculumItemEntity>>

    @Query("SELECT * FROM curriculum_items WHERE roadmapId = :roadmapId")
    fun getCurriculumItemsForRoadmap(roadmapId: String): Flow<List<CurriculumItemEntity>>

    @Query("SELECT * FROM curriculum_items WHERE id = :id")
    suspend fun getCurriculumItemById(id: String): CurriculumItemEntity?

    @Query("UPDATE curriculum_items SET status = :status WHERE id = :id")
    suspend fun updateCurriculumItemStatus(id: String, status: String)
}
