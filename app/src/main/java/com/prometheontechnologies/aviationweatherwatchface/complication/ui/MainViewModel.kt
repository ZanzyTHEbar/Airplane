package com.prometheontechnologies.aviationweatherwatchface.complication.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.UserPreferencesRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingItem
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingsContextualActions
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.UserPreferences
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.createSettingsList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime


class UserPreferencesViewModelFactory(private val repository: UserPreferencesRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }

    sealed class SettingsUIState {
        data object Initial : SettingsUIState()
        data object Loading : SettingsUIState()
        data class Error(val error: String) : SettingsUIState()
        data class SettingsLoaded(val settings: List<SettingItem>) : SettingsUIState()
    }

    var contextualActions: SettingsContextualActions? = null

    private val _uiState = MutableStateFlow<SettingsUIState>(SettingsUIState.Initial)
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    private val _locationServicesButtonEnabled = MutableStateFlow(false)
    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)

    //* Call from the MainActivity or compose UI
    val locationServicesButtonEnabled: StateFlow<Boolean> =
        _locationServicesButtonEnabled.asStateFlow()
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    init {
        viewModelScope.launch {
            repository.readUserPreferences().collect { preferences ->
                _userPreferences.value = preferences
            }
        }
        loadSettings()
    }

    override fun onCleared() {
        super.onCleared()
        contextualActions = null
    }

    /***********************************************************************************************
     ***************************************** UI State Methods ************************************
     ***********************************************************************************************/

    private fun loadSettings() = viewModelScope.launch {
        try {
            val settings = createSettingsList()
            _uiState.value = SettingsUIState.SettingsLoaded(settings)
        } catch (e: Exception) {
            _uiState.value = SettingsUIState.Error(e.message ?: "Unknown Error")
        }
    }

    fun updateLocationServicesButton(enabled: Boolean) {
        _locationServicesButtonEnabled.value = enabled
    }

    fun updateSwitchesSetting(settingId: Int, checked: Boolean) = viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState !is SettingsUIState.SettingsLoaded) return@launch
        val settingToUpdate = currentState.settings.find { it.id == settingId } ?: return@launch

        if (settingToUpdate.id != settingId && !settingToUpdate.enabled) {
            return@launch
        }

        val updatedSetting = settingToUpdate.copy(
            checked = checked
        )

        val updatedSettings = currentState.settings.map { setting ->
            when (setting.id) {
                settingId -> updatedSetting
                else -> setting
            }
        }

        updateSettingsUIState(SettingsUIState.SettingsLoaded(updatedSettings))

        Log.v(
            TAG,
            "Setting: $updatedSetting"
        )

        when (settingId) {
            0 -> handleLocationService(checked)
            2 -> updateFlyMode(checked)
            3 -> updateEnableMilitary(checked)
            else -> {}
        }
    }

    private fun updateSettingsUIState(newState: SettingsUIState) {
        _uiState.value = newState
    }

    private fun saveUserPreference(userPreference: UserPreferences) {
        viewModelScope.launch {
            repository.saveUserPreference(userPreference)
        }
    }

    fun handleLocationService(checked: Boolean) = viewModelScope.launch {
        contextualActions?.toggleLocationService(checked)
    }

    fun scheduleWeatherUpdates(context: Context) {
        contextualActions?.scheduleWeatherUpdates(context)
    }

    /***********************************************************************************************
     ***************************************** DataStore State Methods *****************************
     ***********************************************************************************************/

    private fun updateEnableMilitary(checked: Boolean) = viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState !is SettingsUIState.SettingsLoaded) return@launch
        val updatedUserPreferences = _userPreferences.value?.copy(
            enableMilitary = checked
        ) ?: return@launch

        LocalDataRepository.updateMilitaryEnabled(checked)
        saveUserPreference(updatedUserPreferences)
        restartService()
    }

    private fun updateFlyMode(checked: Boolean) = viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState !is SettingsUIState.SettingsLoaded) return@launch
        val interval = if (checked) 2 else 15
        val updatedUserPreferences = _userPreferences.value?.copy(
            flyingMode = checked,
            updatePeriod = interval
        ) ?: return@launch

        LocalDataRepository.updateUpdateInterval(interval)
        saveUserPreference(updatedUserPreferences)
        restartService()
    }


    fun updateIntervalSetting(time: LocalTime) = viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState !is SettingsUIState.SettingsLoaded) return@launch

        val interval = if (time.hour == 0) {
            time.minute
        } else {
            time.hour * 60
        }

        val updatedUserPreferences = _userPreferences.value?.copy(
            updatePeriod = interval
        ) ?: return@launch

        Log.v(
            TAG,
            "MainViewModel Interval: $interval"
        )

        LocalDataRepository.updateUpdateInterval(interval)
        saveUserPreference(updatedUserPreferences)
        restartService()
    }

    private suspend fun restartService() {
        handleLocationService(false)
        delay(500)
        handleLocationService(true)
    }
}