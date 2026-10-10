package com.stillwellbeing.app.ui.insights

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stillwellbeing.app.domain.DayRecord
import com.stillwellbeing.app.domain.PersonalRecords
import com.stillwellbeing.app.repository.TrackingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "InsightsViewModel"

sealed interface InsightsUiState {
    data object Loading : InsightsUiState
    data object NeedsPermission : InsightsUiState
    data class Loaded(val last7Days: List<DayRecord>, val records: PersonalRecords) : InsightsUiState
    data class Failed(val message: String) : InsightsUiState
}

class InsightsViewModel(private val trackingRepository: TrackingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<InsightsUiState>(InsightsUiState.Loading)
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                if (!trackingRepository.hasUsageAccess()) {
                    _uiState.value = InsightsUiState.NeedsPermission
                    return@launch
                }
                val last7Days = trackingRepository.getLast7Days()
                val records = trackingRepository.getPersonalRecords()
                _uiState.value = InsightsUiState.Loaded(last7Days, records)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load insights", e)
                _uiState.value = InsightsUiState.Failed(e.message ?: e::class.java.simpleName)
            }
        }
    }
}
