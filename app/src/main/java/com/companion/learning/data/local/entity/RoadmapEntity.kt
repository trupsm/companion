package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roadmaps")
data class RoadmapEntity(
    @PrimaryKey val id: String,
    val title: String,
    val goal: String,
    val duration: String,
    val experienceLevel: String,
    val hoursPerDay: Int,
    val llmProviderId: String,
    val schemaVersion: Int,
    val status: String, // GENERATING_SKELETON, NOT_YET_STARTED, ACTIVE, ARCHIVED
    val startedAt: Long?,
    val createdAt: Long
)
