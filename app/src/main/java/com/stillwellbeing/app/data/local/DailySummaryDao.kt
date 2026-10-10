package com.stillwellbeing.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DailySummaryDao {

    @Upsert
    suspend fun upsert(entity: DailySummaryEntity)

    @Query("SELECT * FROM daily_summary WHERE dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getByDate(dateEpochDay: Long): DailySummaryEntity?

    @Query(
        "SELECT * FROM daily_summary WHERE dateEpochDay BETWEEN :startEpochDay AND :endEpochDay " +
            "ORDER BY dateEpochDay ASC"
    )
    suspend fun getRange(startEpochDay: Long, endEpochDay: Long): List<DailySummaryEntity>

    @Query(
        "SELECT * FROM daily_summary WHERE isComplete = 1 " +
            "ORDER BY allDayLongestBreakMs DESC LIMIT 1"
    )
    suspend fun getLongestAllDayBreak(): DailySummaryEntity?

    @Query(
        "SELECT * FROM daily_summary WHERE isComplete = 1 " +
            "ORDER BY wakingLongestBreakMs DESC LIMIT 1"
    )
    suspend fun getLongestWakingBreak(): DailySummaryEntity?

    @Query(
        "SELECT * FROM daily_summary WHERE isComplete = 1 " +
            "ORDER BY allDayPickups ASC LIMIT 1"
    )
    suspend fun getFewestAllDayPickups(): DailySummaryEntity?

    @Query(
        "SELECT * FROM daily_summary WHERE isComplete = 1 " +
            "ORDER BY wakingPickups ASC LIMIT 1"
    )
    suspend fun getFewestWakingPickups(): DailySummaryEntity?

    @Query("SELECT MAX(dateEpochDay) FROM daily_summary WHERE isComplete = 1")
    suspend fun getLastFinalizedEpochDay(): Long?

    @Query("DELETE FROM daily_summary")
    suspend fun deleteAll()
}
