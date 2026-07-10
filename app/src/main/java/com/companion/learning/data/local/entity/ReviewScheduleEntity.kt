package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "review_schedules",
    foreignKeys = [
        ForeignKey(
            entity = CurriculumItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["curriculumItemId"],
            onDelete = ForeignKey.SET_NULL // Orphan them, not cascade delete, based on spec
        )
    ],
    indices = [Index("curriculumItemId")]
)
data class ReviewScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val curriculumItemId: String?, // Nullable because it can be orphaned
    val lastReviewed: Long,
    val nextReview: Long,
    val status: String // ACTIVE, ORPHANED, COMPLETED
)
