package com.companion.learning.domain.repository

import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.QuizQuestionEntity

interface QuizRepository {
    suspend fun generateAndSaveQuiz(item: CurriculumItemEntity, roadmapGoal: String): Result<List<QuizQuestionEntity>>
    suspend fun getQuizForItem(curriculumItemId: String): List<QuizQuestionEntity>
    suspend fun hasQuizForItem(curriculumItemId: String): Boolean
}
