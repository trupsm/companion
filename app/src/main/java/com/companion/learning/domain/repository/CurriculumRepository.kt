package com.companion.learning.domain.repository

import com.companion.learning.data.local.entity.CurriculumItemEntity
import kotlinx.coroutines.flow.Flow

interface CurriculumRepository {
    suspend fun insertCurriculumItems(items: List<CurriculumItemEntity>)
    fun getCurriculumItemsForDay(dayNumber: Int): Flow<List<CurriculumItemEntity>>
    fun getCurriculumItemsForRoadmap(roadmapId: String): Flow<List<CurriculumItemEntity>>
    suspend fun getCurriculumItemById(id: String): CurriculumItemEntity?
    suspend fun updateCurriculumItemStatus(id: String, status: String)
    suspend fun markTopicInProgress(id: String)
}
