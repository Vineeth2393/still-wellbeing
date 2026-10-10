package com.stillwellbeing.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stillwellbeing.app.domain.Goals
import com.stillwellbeing.app.domain.RecordWindow
import com.stillwellbeing.app.domain.ThemeChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "still_settings")

/**
 * Every setting is optional and local-only (DataStore -> a file under
 * /data/data/com.stillwellbeing.app — never leaves the device).
 */
class SettingsRepository(private val appContext: Context) {

    private object Keys {
        val PICKUP_GOAL = intPreferencesKey("pickup_goal")
        val SCREEN_TIME_GOAL_MIN = intPreferencesKey("screen_time_goal_minutes")
        val BREAK_GOAL_MIN = intPreferencesKey("break_goal_minutes")
        val RECORD_WINDOW = stringPreferencesKey("record_window")
        val WAKING_START_MIN = intPreferencesKey("waking_start_minute")
        val WAKING_END_MIN = intPreferencesKey("waking_end_minute")
        val THEME_CHOICE = stringPreferencesKey("theme_choice")
        val WEEKLY_SUMMARY_ENABLED = booleanPreferencesKey("weekly_summary_enabled")
        val TRACKING_STARTED_AT_MS = longPreferencesKey("tracking_started_at_ms")
        val APP_LIST_SIZE = intPreferencesKey("app_list_size")
    }

    val preferences: Flow<UserPreferences> = appContext.dataStore.data.map { prefs ->
        UserPreferences(
            goals = Goals(
                pickupGoal = prefs[Keys.PICKUP_GOAL],
                screenTimeGoalMinutes = prefs[Keys.SCREEN_TIME_GOAL_MIN],
                breakGoalMinutes = prefs[Keys.BREAK_GOAL_MIN]
            ),
            recordWindow = prefs[Keys.RECORD_WINDOW]
                ?.let { runCatching { RecordWindow.valueOf(it) }.getOrNull() }
                ?: UserPreferences().recordWindow,
            wakingStartMinute = prefs[Keys.WAKING_START_MIN]
                ?: UserPreferences.DEFAULT_WAKING_START_MINUTE,
            wakingEndMinute = prefs[Keys.WAKING_END_MIN]
                ?: UserPreferences.DEFAULT_WAKING_END_MINUTE,
            themeChoice = prefs[Keys.THEME_CHOICE]
                ?.let { runCatching { ThemeChoice.valueOf(it) }.getOrNull() }
                ?: ThemeChoice.SYSTEM,
            weeklySummaryEnabled = prefs[Keys.WEEKLY_SUMMARY_ENABLED] ?: false,
            appListSize = prefs[Keys.APP_LIST_SIZE] ?: UserPreferences.DEFAULT_APP_LIST_SIZE,
            trackingStartedAtMs = prefs[Keys.TRACKING_STARTED_AT_MS]
        )
    }

    suspend fun setPickupGoal(value: Int?) = appContext.dataStore.edit {
        if (value == null) it.remove(Keys.PICKUP_GOAL) else it[Keys.PICKUP_GOAL] = value
    }

    suspend fun setScreenTimeGoalMinutes(value: Int?) = appContext.dataStore.edit {
        if (value == null) it.remove(Keys.SCREEN_TIME_GOAL_MIN) else it[Keys.SCREEN_TIME_GOAL_MIN] = value
    }

    suspend fun setBreakGoalMinutes(value: Int?) = appContext.dataStore.edit {
        if (value == null) it.remove(Keys.BREAK_GOAL_MIN) else it[Keys.BREAK_GOAL_MIN] = value
    }

    suspend fun setRecordWindow(value: RecordWindow) = appContext.dataStore.edit {
        it[Keys.RECORD_WINDOW] = value.name
    }

    suspend fun setWakingHours(startMinute: Int, endMinute: Int) = appContext.dataStore.edit {
        it[Keys.WAKING_START_MIN] = startMinute
        it[Keys.WAKING_END_MIN] = endMinute
    }

    suspend fun setAppListSize(value: Int) = appContext.dataStore.edit {
        it[Keys.APP_LIST_SIZE] = value
    }

    suspend fun setThemeChoice(value: ThemeChoice) = appContext.dataStore.edit {
        it[Keys.THEME_CHOICE] = value.name
    }

    suspend fun setWeeklySummaryEnabled(value: Boolean) = appContext.dataStore.edit {
        it[Keys.WEEKLY_SUMMARY_ENABLED] = value
    }

    /** No-ops if already set — tracking-start is recorded once, ever. */
    suspend fun markTrackingStartedIfNeeded(nowMs: Long) = appContext.dataStore.edit {
        if (it[Keys.TRACKING_STARTED_AT_MS] == null) {
            it[Keys.TRACKING_STARTED_AT_MS] = nowMs
        }
    }
}
