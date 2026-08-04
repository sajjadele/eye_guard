package com.example.eyeguard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eyeguard.domain.models.BreakEndAlertType
import com.example.eyeguard.domain.models.DailyStats
import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.domain.repository.SettingsRepository
import com.example.eyeguard.domain.repository.StatsRepository
import com.example.eyeguard.domain.usecases.ObserveSettingsUseCase
import com.example.eyeguard.domain.usecases.UpdateSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class MainUiState(
    val settings: EyeGuardSettings = EyeGuardSettings(),
    val dailyStats: DailyStats = DailyStats.EMPTY,
    val isLoading: Boolean = true
)

class EyeGuardViewModel(
    private val observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
            .onEach { settings ->
                _uiState.value = _uiState.value.copy(
                    settings = settings,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)

        refreshDailyStats()
    }

    fun refreshDailyStats() {
        viewModelScope.launch {
            val stats = statsRepository.getDailyStats()
            _uiState.value = _uiState.value.copy(dailyStats = stats)
        }
    }

    fun setWorkInterval(minutes: Int) {
        update { it.copy(workIntervalMinutes = minutes) }
    }

    fun setBreakDuration(seconds: Int) {
        update { it.copy(breakDurationSeconds = seconds) }
    }

    fun setBreakEndAlertType(type: BreakEndAlertType) {
        update { it.copy(breakEndAlertType = type) }
    }

    fun setEnabled(enabled: Boolean) {
        update { it.copy(enabled = enabled) }
    }

    private fun update(transform: (EyeGuardSettings) -> EyeGuardSettings) {
        viewModelScope.launch {
            updateSettings(transform)
        }
    }
}

class EyeGuardViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EyeGuardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EyeGuardViewModel(
                ObserveSettingsUseCase(settingsRepository),
                UpdateSettingsUseCase(settingsRepository),
                statsRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
