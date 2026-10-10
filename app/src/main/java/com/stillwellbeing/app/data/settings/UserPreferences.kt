package com.stillwellbeing.app.data.settings

import com.stillwellbeing.app.domain.Goals
import com.stillwellbeing.app.domain.RecordWindow
import com.stillwellbeing.app.domain.ThemeChoice

/** Snapshot of everything the user controls — all optional, nothing enforced. */
data class UserPreferences(
    val goals: Goals = Goals(),
    // Waking-hours is the default: an untouched overnight sleep gap shouldn't
    // walk away with "longest break" by default — see DEFAULT_WAKING_* below.
    val recordWindow: RecordWindow = RecordWindow.WAKING_HOURS,
    val wakingStartMinute: Int = DEFAULT_WAKING_START_MINUTE,
    val wakingEndMinute: Int = DEFAULT_WAKING_END_MINUTE,
    val themeChoice: ThemeChoice = ThemeChoice.SYSTEM,
    val weeklySummaryEnabled: Boolean = false,
    /** How many apps "Time in your apps" lists on Today; [ALL_APPS] (0) lists every app used. */
    val appListSize: Int = DEFAULT_APP_LIST_SIZE,
    /** Set once, the first time usage access is confirmed granted. Powers the
     *  "tracking started partway through today" honest state. Null = never granted yet. */
    val trackingStartedAtMs: Long? = null
) {
    companion object {
        const val ALL_APPS = 0
        const val DEFAULT_APP_LIST_SIZE = 5
        val APP_LIST_SIZE_OPTIONS = listOf(3, 5, 10, ALL_APPS)
        const val DEFAULT_WAKING_START_MINUTE = 5 * 60        // 5:00 AM
        const val DEFAULT_WAKING_END_MINUTE = 23 * 60 + 30    // 11:30 PM
    }
}
