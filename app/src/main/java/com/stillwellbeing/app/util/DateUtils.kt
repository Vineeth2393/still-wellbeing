package com.stillwellbeing.app.util

import java.time.LocalDate
import java.time.ZoneId

/**
 * Every function here works in the device's current default zone. Digital
 * habits are inherently zone-local — "today" means the user's calendar day,
 * not UTC's — so we deliberately avoid storing or comparing raw UTC instants
 * without going through these helpers.
 */
object DateUtils {

    fun today(zone: ZoneId = ZoneId.systemDefault()): LocalDate = LocalDate.now(zone)

    /** Epoch-millis start of [date], inclusive. */
    fun startOfDayMs(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    /** Epoch-millis start of the day after [date] — i.e. the exclusive end of [date]. */
    fun endOfDayMs(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Long =
        date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

    /** [date]'s range, clamped so it never extends into the future. */
    fun dayRangeMs(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): LongRange {
        val start = startOfDayMs(date, zone)
        val end = endOfDayMs(date, zone).coerceAtMost(System.currentTimeMillis())
        return start..end
    }

    /**
     * A same-day "waking hours" window, e.g. 7:00–23:00. Only supports windows
     * that don't cross midnight — matching how the Settings screen presents it
     * ("count breaks during: all day / selected waking hours").
     */
    fun wakingHoursRangeMs(
        date: LocalDate,
        startMinuteOfDay: Int,
        endMinuteOfDay: Int,
        zone: ZoneId = ZoneId.systemDefault()
    ): LongRange {
        val dayStart = startOfDayMs(date, zone)
        val start = dayStart + startMinuteOfDay.coerceIn(0, 1440) * 60_000L
        val end = (dayStart + endMinuteOfDay.coerceIn(0, 1440) * 60_000L)
            .coerceAtMost(System.currentTimeMillis())
        return start..end.coerceAtLeast(start)
    }

    /** The last [days] calendar days including today, oldest first. */
    fun lastDays(days: Int, zone: ZoneId = ZoneId.systemDefault()): List<LocalDate> {
        val today = today(zone)
        return (days - 1 downTo 0).map { today.minusDays(it.toLong()) }
    }
}
