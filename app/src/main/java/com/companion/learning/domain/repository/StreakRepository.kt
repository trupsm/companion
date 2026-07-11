package com.companion.learning.domain.repository

interface StreakRepository {
    suspend fun getActiveStreak(): Int
    suspend fun recordStudySessionToday(): Int
    suspend fun claimGraceDayToday(): Boolean
    fun getAvailableGraceDays(): Int
    fun setAvailableGraceDays(count: Int)
}
