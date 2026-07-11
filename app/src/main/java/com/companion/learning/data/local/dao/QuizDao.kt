package com.companion.learning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.companion.learning.data.local.entity.QuizQuestionEntity

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestions(questions: List<QuizQuestionEntity>)

    @Query("SELECT * FROM quiz_questions WHERE curriculumItemId = :curriculumItemId")
    suspend fun getQuestionsForItem(curriculumItemId: String): List<QuizQuestionEntity>

    @Query("SELECT COUNT(*) > 0 FROM quiz_questions WHERE curriculumItemId = :curriculumItemId")
    suspend fun hasQuestionsForItem(curriculumItemId: String): Boolean
}
