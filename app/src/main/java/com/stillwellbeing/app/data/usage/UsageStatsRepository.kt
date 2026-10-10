package com.stillwellbeing.app.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import com.stillwellbeing.app.domain.AppUsageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The only place in the app that talks to Android's usage-access APIs.
 *
 * Pickup definition: a "pickup" is a [UsageEvents.Event.KEYGUARD_HIDDEN] event
 * — the moment the lock screen goes away. This is what Android's own Digital
 * Wellbeing uses, it's provided by the system (no background service or
 * battery cost on our end), and it matches what we tell the user in the app
 * ("A pickup means a phone unlock"). It requires Android 10 (API 29)+.
 */
class UsageStatsRepository(private val appContext: Context) {

    private companion object {
        /** Pause→resume gaps shorter than this are the same visit to an app. */
        const val SAME_SESSION_GAP_MS = 1_500L
    }

    fun hasUsageAccess(): Boolean {
        val appOps = appContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            appContext.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Unlock timestamps (epoch millis) in [start, end), unsorted. */
    suspend fun queryUnlockTimestamps(start: Long, end: Long): List<Long> =
        withContext(Dispatchers.Default) {
            if (end <= start || !hasUsageAccess()) return@withContext emptyList()

            val usageStatsManager =
                appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val events = usageStatsManager.queryEvents(start, end)
            val timestamps = mutableListOf<Long>()
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN) {
                    timestamps += event.timeStamp
                }
            }
            timestamps
        }

    /** Per-app foreground time in [start, end), for user-facing apps only, longest first. */
    suspend fun queryAppUsage(start: Long, end: Long): List<AppUsageItem> =
        withContext(Dispatchers.Default) {
            if (end <= start || !hasUsageAccess()) return@withContext emptyList()

            val usageStatsManager =
                appContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val aggregated = usageStatsManager.queryAndAggregateUsageStats(start, end)
            val packageManager = appContext.packageManager
            val opensByPackage = countOpens(usageStatsManager, start, end)

            aggregated.values
                .filter { it.totalTimeInForeground > 0 && it.packageName != appContext.packageName }
                .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
                .map { stats ->
                    AppUsageItem(
                        packageName = stats.packageName,
                        label = resolveLabel(packageManager, stats.packageName),
                        totalTimeMs = stats.totalTimeInForeground,
                        opens = opensByPackage[stats.packageName] ?: 0
                    )
                }
                .sortedByDescending { it.totalTimeMs }
        }

    /**
     * How many separate times each app was opened in [start, end). An app
     * counts as "opened" when one of its screens resumes while the app wasn't
     * already in the foreground. Moving between two screens of the same app
     * (one pauses, the next resumes within [SAME_SESSION_GAP_MS]) is not a new open.
     */
    private fun countOpens(usageStatsManager: UsageStatsManager, start: Long, end: Long): Map<String, Int> {
        val events = usageStatsManager.queryEvents(start, end)
        val event = UsageEvents.Event()
        val opens = mutableMapOf<String, Int>()
        val inForeground = mutableSetOf<String>()
        val lastPausedAt = mutableMapOf<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    val pausedAt = lastPausedAt[pkg]
                    val freshOpen = pkg !in inForeground &&
                        (pausedAt == null || event.timeStamp - pausedAt > SAME_SESSION_GAP_MS)
                    if (freshOpen) opens[pkg] = (opens[pkg] ?: 0) + 1
                    inForeground += pkg
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    inForeground -= pkg
                    lastPausedAt[pkg] = event.timeStamp
                }
            }
        }
        return opens
    }

    private fun resolveLabel(packageManager: PackageManager, packageName: String): String =
        try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
}
