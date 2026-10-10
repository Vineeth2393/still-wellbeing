package com.stillwellbeing.app.ui.today

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stillwellbeing.app.data.settings.SettingsRepository
import com.stillwellbeing.app.domain.Goals
import com.stillwellbeing.app.domain.TodaySummary
import com.stillwellbeing.app.repository.TrackingRepository
import com.stillwellbeing.app.widget.updateAllStillWidgets
import com.stillwellbeing.app.worker.WorkScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "TodayViewModel"

sealed interface TodayUiState {
    data object Loading : TodayUiState
    data object NeedsPermission : TodayUiState
    data class Loaded(
        val summary: TodaySummary,
        val goals: Goals,
        val personalBestBreakMs: Long?,
        val yesterdayPickups: Int?
    ) : TodayUiState
    /** Something threw while loading — shown instead of leaving the screen blank forever. */
    data class Failed(val message: String) : TodayUiState
}

class TodayViewModel(
    private val trackingRepository: TrackingRepository,
    private val settingsRepository: SettingsRepository,
    private val workContext: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<TodayUiState>(TodayUiState.Loading)
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    /** Re-checks permission and re-queries live usage data. Call on resume — the
     *  user may just have come back from granting usage access in Settings. */
    fun refresh() {
        viewModelScope.launch {
            try {
                if (!trackingRepository.hasUsageAccess()) {
                    _uiState.value = TodayUiState.NeedsPermission
                    return@launch
                }
                trackingRepository.markTrackingStartedIfNeeded()
                WorkScheduler.runNow(workContext)

                val goals = settingsRepository.preferences.first().goals
                val summary = trackingRepository.getTodaySummary()
                val personalBestBreakMs = trackingRepository.getPersonalRecords().longestBreak?.longestBreakMs
                val yesterdayPickups = trackingRepository.getYesterday()?.pickups
                _uiState.value = TodayUiState.Loaded(summary, goals, personalBestBreakMs, yesterdayPickups)
                updateAllStillWidgets(workContext)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load today's summary", e)
                _uiState.value = TodayUiState.Failed(e.message ?: e::class.java.simpleName)
            }
        }
    }
}
