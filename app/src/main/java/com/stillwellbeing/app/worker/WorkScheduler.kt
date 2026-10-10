package com.stillwellbeing.app.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkScheduler {
    private const val PERIODIC_WORK_NAME = "daily_rollover_periodic"
    private const val ONE_TIME_WORK_NAME = "daily_rollover_now"

    /** Called once from [com.stillwellbeing.app.StillApplication.onCreate]. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<DailyRolloverWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Called right after usage access is confirmed granted, to backfill immediately. */
    fun runNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<DailyRolloverWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
