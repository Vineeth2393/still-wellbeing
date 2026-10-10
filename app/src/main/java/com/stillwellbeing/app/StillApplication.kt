package com.stillwellbeing.app

import android.app.Application
import com.stillwellbeing.app.data.local.StillDatabase
import com.stillwellbeing.app.data.settings.SettingsRepository
import com.stillwellbeing.app.data.usage.UsageStatsRepository
import com.stillwellbeing.app.repository.TrackingRepository
import com.stillwellbeing.app.worker.WorkScheduler

/**
 * A hand-rolled service locator instead of a DI framework (Hilt/Koin) —
 * there's exactly one instance of everything here, so a framework would add
 * ceremony without adding safety. Every property is a `by lazy`, so nothing
 * is built until first used.
 */
class StillApplication : Application() {

    val database: StillDatabase by lazy { StillDatabase.getInstance(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    private val usageStatsRepository: UsageStatsRepository by lazy { UsageStatsRepository(this) }

    val trackingRepository: TrackingRepository by lazy {
        TrackingRepository(
            usageStatsRepository = usageStatsRepository,
            settingsRepository = settingsRepository,
            dailySummaryDao = database.dailySummaryDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        WorkScheduler.schedulePeriodic(this)
    }
}
