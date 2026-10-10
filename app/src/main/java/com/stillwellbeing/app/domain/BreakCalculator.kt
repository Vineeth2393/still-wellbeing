package com.stillwellbeing.app.domain

/**
 * Turns a raw list of unlock timestamps into phone-free breaks. Pure and
 * timezone-agnostic — callers decide the range (a full day, waking hours
 * only, etc.) and pass already-filtered event times.
 *
 * We never synthesize a trailing "break so far, until now" interval here:
 * opening this app itself requires an unlock, so by the time these events are
 * queried, "now" already has an unlock event at (or a moment after) it. A
 * live ongoing-break timer belongs on the home-screen widget, not here — see
 * the product notes in ui/today/TodayScreen.kt.
 */
object BreakCalculator {

    /**
     * @param unlockTimesMs sorted or unsorted unlock timestamps, any that fall
     *   outside [rangeStart, rangeEnd] are ignored.
     */
    fun breaksWithin(
        unlockTimesMs: List<Long>,
        rangeStart: Long,
        rangeEnd: Long
    ): List<BreakInterval> {
        val sorted = unlockTimesMs.filter { it in rangeStart..rangeEnd }.sorted()
        if (sorted.isEmpty()) return emptyList()

        val breaks = mutableListOf<BreakInterval>()
        if (sorted.first() > rangeStart) {
            breaks += BreakInterval(rangeStart, sorted.first())
        }
        for (i in 0 until sorted.size - 1) {
            breaks += BreakInterval(sorted[i], sorted[i + 1])
        }
        return breaks
    }

    fun longestBreak(breaks: List<BreakInterval>): BreakInterval? =
        breaks.maxByOrNull { it.durationMs }

    /** The break that ended at the most recent unlock in range — "Your last break". */
    fun lastCompletedBreak(breaks: List<BreakInterval>): BreakInterval? = breaks.lastOrNull()
}
