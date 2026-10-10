package com.stillwellbeing.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One finalized calendar day, written once by [com.stillwellbeing.app.worker.DailyRolloverWorker]
 * after that day has fully elapsed. All-day and waking-hours figures are kept
 * side by side (rather than picking one at write time) so switching the
 * "record window" setting later never mixes or loses history.
 *
 * Per-app history isn't persisted yet — v1's Insights screen only needs
 * day-level totals. That's deferred to the app-detail-screens milestone.
 */
@Entity(tableName = "daily_summary")
data class DailySummaryEntity(
    @PrimaryKey val dateEpochDay: Long,
    val allDayPickups: Int,
    val allDayLongestBreakMs: Long,
    val wakingPickups: Int,
    val wakingLongestBreakMs: Long,
    val totalScreenTimeMs: Long,
    /** True only once the calendar day has fully elapsed under tracking — see
     *  the "never award a record for an incomplete day" rule. */
    val isComplete: Boolean
)
