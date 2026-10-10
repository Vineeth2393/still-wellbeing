package com.stillwellbeing.app.domain

import java.time.LocalDate

/** How the in-app theme picker (Settings) should resolve against the OS theme. */
enum class ThemeChoice { SYSTEM, LIGHT, DARK }

/** Whether break/record calculations count the full day or only waking hours. */
enum class RecordWindow { ALL_DAY, WAKING_HOURS }

/** User-set, optional daily targets. A null field means "no goal set" — never a default. */
data class Goals(
    val pickupGoal: Int? = null,
    val screenTimeGoalMinutes: Int? = null,
    val breakGoalMinutes: Int? = null
)

/** A single phone-free interval between two unlocks (or between tracking-start/now and an unlock). */
data class BreakInterval(val startMs: Long, val endMs: Long) {
    val durationMs: Long get() = (endMs - startMs).coerceAtLeast(0)
}

/** Usage-access + tracking freshness, so the UI can show honest empty/partial states. */
data class TrackingStatus(
    val hasUsageAccess: Boolean,
    val trackingStartedAtMs: Long?,
    val lastUpdatedMs: Long
)

/** Minutes-level per-app usage for a period (today, or a single historical day). */
data class AppUsageItem(
    val packageName: String,
    val label: String,
    val totalTimeMs: Long,
    /** Times the app was brought to the foreground (a fresh open, not a hop between its own screens). */
    val opens: Int = 0
)

/** Everything the Today screen needs, computed live from UsageStatsManager. */
data class TodaySummary(
    val date: LocalDate,
    val pickups: Int,
    val totalScreenTimeMs: Long,
    val longestBreakToday: BreakInterval?,
    val lastCompletedBreak: BreakInterval?,
    val topApps: List<AppUsageItem>,
    val otherAppsTimeMs: Long,
    val isPartialDay: Boolean,
    val trackingStatus: TrackingStatus
)

/** One finalized, persisted day — the unit that Insights and Records are built from. */
data class DayRecord(
    val date: LocalDate,
    val pickups: Int,
    val totalScreenTimeMs: Long,
    val longestBreakMs: Long,
    val isComplete: Boolean
)

/** All-time bests, computed only from complete tracked days — see [DayRecord.isComplete]. */
data class PersonalRecords(
    val longestBreak: DayRecord?,
    val fewestPickups: DayRecord?
)
