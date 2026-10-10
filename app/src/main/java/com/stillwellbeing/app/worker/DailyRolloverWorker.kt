package com.stillwellbeing.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stillwellbeing.app.StillApplication
import com.stillwellbeing.app.util.DateUtils
import com.stillwellbeing.app.widget.updateAllStillWidgets

/**
 * Finalizes any calendar day that has fully elapsed but isn't in Room yet.
 * Runs periodically (see [WorkScheduler]) rather than exactly at midnight,
 * since a day can only be "complete" once it's actually over — being a few
 * hours late to record that is harmless.
 */
class DailyRolloverWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        /** Safety cap so a very old, never-backfilled install can't loop for ages. */
        private const val MAX_BACKFILL_DAYS = 30L
    }

    override suspend fun doWork(): Result {
        val repository = (applicationContext as StillApplication).trackingRepository
        val today = DateUtils.today()
        val yesterday = today.minusDays(1)

        val lastFinalizedEpochDay =
            (applicationContext as StillApplication).database.dailySummaryDao().getLastFinalizedEpochDay()
        val earliestBackfill = yesterday.minusDays(MAX_BACKFILL_DAYS - 1)
        var date = if (lastFinalizedEpochDay != null) {
            maxOf(java.time.LocalDate.ofEpochDay(lastFinalizedEpochDay + 1), earliestBackfill)
        } else {
            earliestBackfill
        }

        while (!date.isAfter(yesterday)) {
            repository.finalizeDay(date)
            date = date.plusDays(1)
        }

        // A new day rolling over changes "yesterday"/personal-best figures
        // the widget shows, even if the app itself isn't open right now.
        updateAllStillWidgets(applicationContext)

        return Result.success()
    }
}
