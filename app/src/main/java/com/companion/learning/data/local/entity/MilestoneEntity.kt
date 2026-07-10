package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "milestones",
    foreignKeys = [
        ForeignKey(
            entity = RoadmapEntity::class,
            parentColumns = ["id"],
            childColumns = ["roadmapId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("roadmapId")]
)
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val roadmapId: String,
    val weekNumber: Int,
    val title: String,
    val summary: String,
    val expansionStatus: String // PENDING, EXPANDED, FAILED
)
