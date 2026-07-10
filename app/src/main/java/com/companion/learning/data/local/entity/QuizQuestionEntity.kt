package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "quiz_questions",
    foreignKeys = [
        ForeignKey(
            entity = CurriculumItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["curriculumItemId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("curriculumItemId")]
)
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val curriculumItemId: String,
    val question: String,
    val options: String, // Stored as JSON string for now
    val correctAnswer: String
)
