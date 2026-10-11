package com.stillwellbeing.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object TimeFormat {

    /** 135 minutes -> "2h 15m"; 45 minutes -> "45m"; 0 -> "0m". */
    fun duration(ms: Long): String {
        // Real-but-tiny usage reads as "<1m", not "0m" — "0m" looks like none at all.
        if (ms in 1 until 60_000L) return "<1m"
        val totalMinutes = (ms / 60_000L).toInt().coerceAtLeast(0)
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours <= 0 -> "${minutes}m"
            minutes == 0 -> "${hours}h"
            else -> "${hours}h ${minutes}m"
        }
    }

    private val clockFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())

    /** An epoch-millis instant -> "9:10 AM". */
    fun clockTime(epochMs: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        clockFormatter.format(Instant.ofEpochMilli(epochMs).atZone(zone))

    /** A single-letter weekday label for chart axes: Monday -> "M". */
    fun weekdayInitial(date: LocalDate): String =
        date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())

    private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())

    /** A date -> "Tue 6 Oct", for chart readouts. */
    fun dayLabel(date: LocalDate): String = dayLabelFormatter.format(date)

    private val monthDayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())

    /** A date -> "8 September", matching the record-card captions in the design. */
    fun monthDay(date: LocalDate): String = monthDayFormatter.format(date)
}
