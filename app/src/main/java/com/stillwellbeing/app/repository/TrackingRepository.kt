package com.stillwellbeing.app.repository

import com.stillwellbeing.app.data.local.DailySummaryDao
import com.stillwellbeing.app.data.local.DailySummaryEntity
import com.stillwellbeing.app.data.settings.SettingsRepository
import com.stillwellbeing.app.data.settings.UserPreferences
import com.stillwellbeing.app.data.usage.UsageStatsRepository
import com.stillwellbeing.app.domain.AppUsageItem
import com.stillwellbeing.app.domain.BreakCalculator
import com.stillwellbeing.app.domain.BreakInterval
import com.stillwellbeing.app.domain.DayRecord
import com.stillwellbeing.app.domain.PersonalRecords
import com.stillwellbeing.app.domain.RecordWindow
import com.stillwellbeing.app.domain.TodaySummary
import com.stillwellbeing.app.domain.TrackingStatus
import com.stillwellbeing.app.util.DateUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/**
 * The single orchestrator the ViewModels talk to. It combines live system
 * data ([UsageStatsRepository]) with user settings and local history, and is
 * the one place that decides what "today," "a break," and "a complete day"
 * mean end to end.
 */
class TrackingRepository(
    private val usageStatsRepository: UsageStatsRepository,
    private val settingsRepository: SettingsRepository,
    private val dailySummaryDao: DailySummaryDao
) {

    suspend fun hasUsageAccess(): Boolean = usageStatsRepository.hasUsageAccess()

    /** Call once usage access is confirmed granted; a no-op after the first time. */
    suspend fun markTrackingStartedIfNeeded() {
        settingsRepository.markTrackingStartedIfNeeded(System.currentTimeMillis())
    }

    suspend fun getTrackingStatus(): TrackingStatus {
        val prefs = settingsRepository.preferences.first()
        return TrackingStatus(
            hasUsageAccess = usageStatsRepository.hasUsageAccess(),
            trackingStartedAtMs = prefs.trackingStartedAtMs,
            lastUpdatedMs = System.currentTimeMillis()
        )
    }

    suspend fun getTodaySummary(): TodaySummary {
        val prefs = settingsRepository.preferences.first()
        val now = System.currentTimeMillis()
        val today = DateUtils.today()
        val dayStart = DateUtils.startOfDayMs(today)
        val trackingStartedAtMs = prefs.trackingStartedAtMs

        if (!usageStatsRepository.hasUsageAccess()) {
            return emptyTodaySummary(today, now, trackingStartedAtMs)
        }

        // If tracking only began partway through today, don't pretend the
        // morning happened — start the window at trackingStartedAtMs instead.
        val effectiveDayStart =
            if (trackingStartedAtMs != null && trackingStartedAtMs > dayStart) trackingStartedAtMs
            else dayStart
        val isPartialDay = effectiveDayStart > dayStart

        val allDayUnlocks = usageStatsRepository.queryUnlockTimestamps(effectiveDayStart, now)
        val pickups = allDayUnlocks.size

        val allDayBreaks = BreakCalculator.breaksWithin(allDayUnlocks, effectiveDayStart, now)
        val selectedBreaks = when (prefs.recordWindow) {
            RecordWindow.ALL_DAY -> allDayBreaks
            RecordWindow.WAKING_HOURS -> wakingBreaksFrom(allDayUnlocks, today, prefs, effectiveDayStart, now)
        }

        val appUsage = usageStatsRepository.queryAppUsage(effectiveDayStart, now)
        val totalScreenTimeMs = appUsage.sumOf { it.totalTimeMs }
        val topApps = if (prefs.appListSize == UserPreferences.ALL_APPS) appUsage else appUsage.take(prefs.appListSize)
        val otherAppsTimeMs = (totalScreenTimeMs - topApps.sumOf { it.totalTimeMs }).coerceAtLeast(0)

        return TodaySummary(
            date = today,
            pickups = pickups,
            totalScreenTimeMs = totalScreenTimeMs,
            longestBreakToday = BreakCalculator.longestBreak(selectedBreaks),
            lastCompletedBreak = BreakCalculator.lastCompletedBreak(selectedBreaks),
            topApps = topApps,
            otherAppsTimeMs = otherAppsTimeMs,
            isPartialDay = isPartialDay,
            trackingStatus = TrackingStatus(
                hasUsageAccess = true,
                trackingStartedAtMs = trackingStartedAtMs,
                lastUpdatedMs = now
            )
        )
    }

    /** The last (up to) 7 fully-elapsed, tracked days — never includes today. */
    suspend fun getLast7Days(): List<DayRecord> {
        val prefs = settingsRepository.preferences.first()
        val today = DateUtils.today()
        val start = today.minusDays(7).toEpochDay()
        val end = today.minusDays(1).toEpochDay()
        return dailySummaryDao.getRange(start, end).map { it.toDayRecord(prefs.recordWindow) }
    }

    suspend fun getPersonalRecords(): PersonalRecords {
        val prefs = settingsRepository.preferences.first()
        val (longestBreakEntity, fewestPickupsEntity) = when (prefs.recordWindow) {
            RecordWindow.ALL_DAY ->
                dailySummaryDao.getLongestAllDayBreak() to dailySummaryDao.getFewestAllDayPickups()
            RecordWindow.WAKING_HOURS ->
                dailySummaryDao.getLongestWakingBreak() to dailySummaryDao.getFewestWakingPickups()
        }
        return PersonalRecords(
            longestBreak = longestBreakEntity?.toDayRecord(prefs.recordWindow),
            fewestPickups = fewestPickupsEntity?.toDayRecord(prefs.recordWindow)
        )
    }

    suspend fun deleteAllHistory() = dailySummaryDao.deleteAll()

    /** Yesterday's finalized figures, for the Today screen's "N fewer than yesterday" line. */
    suspend fun getYesterday(): DayRecord? {
        val prefs = settingsRepository.preferences.first()
        val yesterday = DateUtils.today().minusDays(1)
        return dailySummaryDao.getByDate(yesterday.toEpochDay())?.toDayRecord(prefs.recordWindow)
    }

    /**
     * Finalizes one already-elapsed day into Room. Idempotent (upsert) — safe
     * to call repeatedly for the same date, which is how the rollover worker
     * backfills any days it missed.
     */
    suspend fun finalizeDay(date: LocalDate) {
        val prefs = settingsRepository.preferences.first()
        val trackingStartedAtMs = prefs.trackingStartedAtMs ?: return // never tracked, nothing to save
        val dayStart = DateUtils.startOfDayMs(date)
        val dayEnd = DateUtils.endOfDayMs(date)
        if (dayEnd > System.currentTimeMillis()) return // day hasn't fully elapsed yet
        if (trackingStartedAtMs >= dayEnd) return // this day predates tracking entirely — nothing to record

        val effectiveStart = if (trackingStartedAtMs > dayStart) trackingStartedAtMs else dayStart
        // A day that only started partway through (installation day) never
        // counts as "complete" — it can't fairly compete for records.
        val isComplete = effectiveStart <= dayStart

        val unlocks = usageStatsRepository.queryUnlockTimestamps(effectiveStart, dayEnd)
        val allDayBreaks = BreakCalculator.breaksWithin(unlocks, effectiveStart, dayEnd)
        val wakingBreaks = wakingBreaksFrom(unlocks, date, prefs, effectiveStart, dayEnd)

        val appUsage = usageStatsRepository.queryAppUsage(effectiveStart, dayEnd)
        val totalScreenTimeMs = appUsage.sumOf { it.totalTimeMs }

        dailySummaryDao.upsert(
            DailySummaryEntity(
                dateEpochDay = date.toEpochDay(),
                allDayPickups = unlocks.size,
                allDayLongestBreakMs = BreakCalculator.longestBreak(allDayBreaks)?.durationMs ?: 0L,
                wakingPickups = unlocks.count { it in wakingRangeOrEmpty(date, prefs, effectiveStart, dayEnd) },
                wakingLongestBreakMs = BreakCalculator.longestBreak(wakingBreaks)?.durationMs ?: 0L,
                totalScreenTimeMs = totalScreenTimeMs,
                isComplete = isComplete
            )
        )
    }

    private fun wakingBreaksFrom(
        unlocks: List<Long>,
        date: LocalDate,
        prefs: UserPreferences,
        dayEffectiveStart: Long,
        dayEffectiveEnd: Long
    ): List<BreakInterval> {
        val range = wakingRangeOrEmpty(date, prefs, dayEffectiveStart, dayEffectiveEnd)
        if (range.isEmpty()) return emptyList()
        return BreakCalculator.breaksWithin(unlocks, range.first, range.last)
    }

    private fun wakingRangeOrEmpty(
        date: LocalDate,
        prefs: UserPreferences,
        dayEffectiveStart: Long,
        dayEffectiveEnd: Long
    ): LongRange {
        val waking = DateUtils.wakingHoursRangeMs(date, prefs.wakingStartMinute, prefs.wakingEndMinute)
        val start = maxOf(dayEffectiveStart, waking.first)
        val end = minOf(dayEffectiveEnd, waking.last)
        return if (end > start) start..end else LongRange.EMPTY
    }

    private fun emptyTodaySummary(
        today: LocalDate,
        now: Long,
        trackingStartedAtMs: Long?
    ) = TodaySummary(
        date = today,
        pickups = 0,
        totalScreenTimeMs = 0L,
        longestBreakToday = null,
        lastCompletedBreak = null,
        topApps = emptyList<AppUsageItem>(),
        otherAppsTimeMs = 0L,
        isPartialDay = false,
        trackingStatus = TrackingStatus(
            hasUsageAccess = false,
            trackingStartedAtMs = trackingStartedAtMs,
            lastUpdatedMs = now
        )
    )

    private fun DailySummaryEntity.toDayRecord(window: RecordWindow) = DayRecord(
        date = LocalDate.ofEpochDay(dateEpochDay),
        pickups = if (window == RecordWindow.ALL_DAY) allDayPickups else wakingPickups,
        totalScreenTimeMs = totalScreenTimeMs,
        longestBreakMs = if (window == RecordWindow.ALL_DAY) allDayLongestBreakMs else wakingLongestBreakMs,
        isComplete = isComplete
    )
}
