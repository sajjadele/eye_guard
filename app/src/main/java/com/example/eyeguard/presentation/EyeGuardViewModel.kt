package com.example.eyeguard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.eyeguard.domain.model.ContentCard
import com.example.eyeguard.domain.models.BreakEndAlertType
import com.example.eyeguard.domain.models.DailyStats
import com.example.eyeguard.domain.models.EyeGuardSettings
import com.example.eyeguard.domain.repository.ContentRepository
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
    val isLoading: Boolean = true,
    val tipsCount: Int = 0,
    val isSyncingTips: Boolean = false,
    val randomTip: ContentCard? = null,
    val syncMessage: String? = null,
    val savedCards: List<ContentCard> = emptyList()
)

class EyeGuardViewModel(
    private val observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val statsRepository: StatsRepository,
    private val contentRepository: ContentRepository
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

        contentRepository.getTipsCount()
            .onEach { count ->
                _uiState.value = _uiState.value.copy(tipsCount = count)
            }
            .launchIn(viewModelScope)

        loadRandomTip()
        syncTips()
    }

    fun refreshDailyStats() {
        viewModelScope.launch {
            val stats = statsRepository.getDailyStats()
            _uiState.value = _uiState.value.copy(dailyStats = stats)
        }
    }

    fun loadRandomTip() {
        viewModelScope.launch {
            val tip = contentRepository.getRandomCard(com.example.eyeguard.domain.model.ContentCategory.EYE_CARE)
            _uiState.value = _uiState.value.copy(randomTip = tip)
        }
    }

    fun syncTips() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncingTips = true, syncMessage = null)
            val result = contentRepository.syncRemoteTips()
            val message = result.fold(
                onSuccess = { addedCount ->
                    if (addedCount > 0) "تعداد $addedCount نکته جدید دریافت شد"
                    else "نکات سلامت چشم به‌روز هستند"
                },
                onFailure = {
                    "حالت آفلاین (استفاده از نکات محلی)"
                }
            )
            _uiState.value = _uiState.value.copy(isSyncingTips = false, syncMessage = message)
            loadRandomTip()
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

    fun setThemeMode(mode: com.example.eyeguard.domain.models.AppThemeMode) {
        update { it.copy(themeMode = mode) }
    }

    fun toggleTheme() {
        val currentMode = _uiState.value.settings.themeMode
        val newMode = if (currentMode == com.example.eyeguard.domain.models.AppThemeMode.LIGHT) {
            com.example.eyeguard.domain.models.AppThemeMode.DARK
        } else {
            com.example.eyeguard.domain.models.AppThemeMode.LIGHT
        }
        setThemeMode(newMode)
    }

    private fun update(transform: (EyeGuardSettings) -> EyeGuardSettings) {
        viewModelScope.launch {
            updateSettings(transform)
        }
    }
}

class EyeGuardViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository,
    private val contentRepository: ContentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EyeGuardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EyeGuardViewModel(
                ObserveSettingsUseCase(settingsRepository),
                UpdateSettingsUseCase(settingsRepository),
                statsRepository,
                contentRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
