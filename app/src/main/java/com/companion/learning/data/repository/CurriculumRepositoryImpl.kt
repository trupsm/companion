package com.companion.learning.data.repository

import com.companion.learning.data.local.dao.CurriculumDao
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.domain.repository.CurriculumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CurriculumRepositoryImpl @Inject constructor(
    private val curriculumDao: CurriculumDao
) : CurriculumRepository {

    override suspend fun insertCurriculumItems(items: List<CurriculumItemEntity>) {
        curriculumDao.insertCurriculumItems(items)
    }

    override fun getCurriculumItemsForDay(dayNumber: Int): Flow<List<CurriculumItemEntity>> {
        return curriculumDao.getCurriculumItemsForDay(dayNumber)
    }

    override fun getCurriculumItemsForRoadmap(roadmapId: String): Flow<List<CurriculumItemEntity>> {
        return curriculumDao.getCurriculumItemsForRoadmap(roadmapId)
    }

    override suspend fun getCurriculumItemById(id: String): CurriculumItemEntity? {
        return curriculumDao.getCurriculumItemById(id)
    }

    override suspend fun updateCurriculumItemStatus(id: String, status: String) {
        curriculumDao.updateCurriculumItemStatus(id, status)
    }
}
