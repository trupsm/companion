package com.companion.learning.data.repository

import com.companion.learning.data.local.dao.QuizDao
import com.companion.learning.data.local.entity.CurriculumItemEntity
import com.companion.learning.data.local.entity.QuizQuestionEntity
import com.companion.learning.domain.provider.LlmProvider
import com.companion.learning.domain.repository.QuizRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao,
    private val llmProvider: LlmProvider
) : QuizRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun generateAndSaveQuiz(
        item: CurriculumItemEntity,
        roadmapGoal: String
    ): Result<List<QuizQuestionEntity>> {
        val result = llmProvider.generateQuiz(
            topic = item.topic,
            description = item.description ?: "",
            goal = roadmapGoal
        )
        return result.map { dtos ->
            val entities = dtos.map { dto ->
                QuizQuestionEntity(
                    id = UUID.randomUUID().toString(),
                    curriculumItemId = item.id,
                    question = dto.question,
                    options = json.encodeToString(dto.options),
                    correctAnswer = dto.correctAnswer
                )
            }
            quizDao.insertQuizQuestions(entities)
            entities
        }
    }

    override suspend fun getQuizForItem(curriculumItemId: String): List<QuizQuestionEntity> {
        return quizDao.getQuestionsForItem(curriculumItemId)
    }

    override suspend fun hasQuizForItem(curriculumItemId: String): Boolean {
        return quizDao.hasQuestionsForItem(curriculumItemId)
    }
}
