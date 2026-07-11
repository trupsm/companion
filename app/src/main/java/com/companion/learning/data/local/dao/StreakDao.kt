package com.companion.learning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.companion.learning.data.local.entity.StreakLogEntity

@Dao
interface StreakDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StreakLogEntity)

    @Query("SELECT * FROM streak_logs ORDER BY date DESC LIMIT 1")
    suspend fun getLatestLog(): StreakLogEntity?

    @Query("SELECT * FROM streak_logs WHERE date = :date")
    suspend fun getLogForDate(date: String): StreakLogEntity?

    @Query("SELECT * FROM streak_logs ORDER BY date DESC")
    suspend fun getAllLogs(): List<StreakLogEntity>
}
