package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "curriculum_items",
    foreignKeys = [
        ForeignKey(
            entity = RoadmapEntity::class,
            parentColumns = ["id"],
            childColumns = ["roadmapId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MilestoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["milestoneId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roadmapId"), Index("milestoneId")]
)
data class CurriculumItemEntity(
    @PrimaryKey val id: String,
    val roadmapId: String,
    val milestoneId: String,
    val dayNumber: Int,
    val topic: String,
    val description: String?,
    val estimatedTime: Int?, // in minutes
    val status: String // NOT_STARTED, IN_PROGRESS, COMPLETED, SKIPPED
)
