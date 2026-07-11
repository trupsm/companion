package com.companion.learning.data.repository

import com.companion.learning.data.local.dao.StreakDao
import com.companion.learning.data.local.entity.StreakLogEntity
import com.companion.learning.data.local.security.SecureStorage
import com.companion.learning.domain.repository.StreakRepository
import java.time.LocalDate
import java.util.TimeZone
import javax.inject.Inject

class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao,
    private val secureStorage: SecureStorage
) : StreakRepository {

    override suspend fun getActiveStreak(): Int {
        val latestLog = streakDao.getLatestLog() ?: return 0
        val today = LocalDate.now()
        val latestLocalDate = LocalDate.parse(latestLog.date)
        
        // If latest log is today or yesterday, streak is active
        return if (latestLocalDate == today || latestLocalDate == today.minusDays(1)) {
            latestLog.streakCount
        } else {
            0
        }
    }

    override suspend fun recordStudySessionToday(): Int {
        val today = LocalDate.now().toString()
        val existingLog = streakDao.getLogForDate(today)
        
        // If already logged today, check if it was a grace day. 
        // If it was a grace day, we overwrite it with a real study session log (maintaining the streak).
        if (existingLog != null && !existingLog.isGraceDay) {
            return existingLog.streakCount
        }

        val latestLog = streakDao.getLatestLog()
        
        val newStreakCount = when {
            latestLog == null -> 1
            // If the latest log is today (and was a grace day) or yesterday, increment
            LocalDate.parse(latestLog.date) == LocalDate.now().minusDays(1) || 
            (LocalDate.parse(latestLog.date) == LocalDate.now() && latestLog.isGraceDay) -> {
                val baseStreak = latestLog.streakCount
                baseStreak + 1
            }
            LocalDate.parse(latestLog.date) == LocalDate.now() && !latestLog.isGraceDay -> {
                // Should not hit this since we check existingLog above, but for safety:
                latestLog.streakCount
            }
            else -> 1 // Streak broken
        }

        val timezoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (60 * 1000)
        val newLog = StreakLogEntity(
            date = today,
            streakCount = newStreakCount,
            timezoneOffsetMinutes = timezoneOffset,
            isGraceDay = false
        )
        streakDao.insertLog(newLog)

        // Award a grace day for completing every 7-day milestone
        if (newStreakCount > 0 && newStreakCount % 7 == 0) {
            val available = getAvailableGraceDays()
            if (available < 3) {
                setAvailableGraceDays(available + 1)
            }
        }

        return newStreakCount
    }

    override suspend fun claimGraceDayToday(): Boolean {
        val available = getAvailableGraceDays()
        if (available <= 0) return false

        val today = LocalDate.now().toString()
        val existingLog = streakDao.getLogForDate(today)
        if (existingLog != null) {
            return false // Already studied or claimed grace day today
        }

        // Get yesterday's streak to preserve it
        val latestLog = streakDao.getLatestLog()
        val yesterday = LocalDate.now().minusDays(1)
        val preservedStreak = if (latestLog != null && 
            (LocalDate.parse(latestLog.date) == yesterday || LocalDate.parse(latestLog.date) == LocalDate.now())) {
            latestLog.streakCount
        } else {
            0 // Streak was already broken
        }

        val timezoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / (60 * 1000)
        val graceLog = StreakLogEntity(
            date = today,
            streakCount = preservedStreak,
            timezoneOffsetMinutes = timezoneOffset,
            isGraceDay = true
        )
        
        streakDao.insertLog(graceLog)
        setAvailableGraceDays(available - 1)
        return true
    }

    override fun getAvailableGraceDays(): Int {
        val stored = secureStorage.getAvailableGraceDays()
        if (stored == -1) {
            secureStorage.setAvailableGraceDays(1)
            return 1
        }
        return stored
    }

    override fun setAvailableGraceDays(count: Int) {
        secureStorage.setAvailableGraceDays(count.coerceIn(0, 3))
    }
}
