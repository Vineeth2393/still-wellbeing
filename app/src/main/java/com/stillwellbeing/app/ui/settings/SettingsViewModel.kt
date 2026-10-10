package com.stillwellbeing.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stillwellbeing.app.data.settings.SettingsRepository
import com.stillwellbeing.app.data.settings.UserPreferences
import com.stillwellbeing.app.domain.RecordWindow
import com.stillwellbeing.app.domain.ThemeChoice
import com.stillwellbeing.app.repository.TrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val trackingRepository: TrackingRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = settingsRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )

    private val _hasUsageAccess = MutableStateFlow(false)
    val hasUsageAccess: StateFlow<Boolean> = _hasUsageAccess.asStateFlow()

    private val _historyCleared = MutableStateFlow(false)
    val historyCleared: StateFlow<Boolean> = _historyCleared.asStateFlow()

    init {
        refreshPermission()
    }

    fun refreshPermission() {
        viewModelScope.launch { _hasUsageAccess.value = trackingRepository.hasUsageAccess() }
    }

    fun setPickupGoal(value: Int?) = viewModelScope.launch { settingsRepository.setPickupGoal(value) }
    fun setScreenTimeGoalMinutes(value: Int?) =
        viewModelScope.launch { settingsRepository.setScreenTimeGoalMinutes(value) }
    fun setBreakGoalMinutes(value: Int?) =
        viewModelScope.launch { settingsRepository.setBreakGoalMinutes(value) }

    fun setRecordWindow(value: RecordWindow) = viewModelScope.launch { settingsRepository.setRecordWindow(value) }
    fun setWakingHours(startMinute: Int, endMinute: Int) =
        viewModelScope.launch { settingsRepository.setWakingHours(startMinute, endMinute) }

    fun setAppListSize(value: Int) = viewModelScope.launch { settingsRepository.setAppListSize(value) }

    fun setThemeChoice(value: ThemeChoice) = viewModelScope.launch { settingsRepository.setThemeChoice(value) }

    fun deleteHistory() {
        viewModelScope.launch {
            trackingRepository.deleteAllHistory()
            _historyCleared.value = true
        }
    }

    fun acknowledgeHistoryCleared() {
        _historyCleared.value = false
    }
}
