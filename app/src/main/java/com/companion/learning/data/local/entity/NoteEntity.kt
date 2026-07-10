package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val roadmapId: String,
    val curriculumItemId: String?,   // nullable: notes can be per-topic or standalone
    val dayDate: String,             // ISO date string "YYYY-MM-DD" for calendar grouping
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long
)
