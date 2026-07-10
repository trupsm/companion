package com.companion.learning.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streak_logs")
data class StreakLogEntity(
    @PrimaryKey val date: String, // YYYY-MM-DD local
    val streakCount: Int,
    val timezoneOffsetMinutes: Int,
    val isGraceDay: Boolean
)
