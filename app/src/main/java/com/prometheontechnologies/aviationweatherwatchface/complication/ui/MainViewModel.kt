package com.prometheontechnologies.aviationweatherwatchface.complication.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingItem
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingsContextualActions
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.createSettingsList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

class MainViewModel() : ViewModel() {

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

    /*val complicationsSettings: StateFlow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(ComplicationsDataSerializer.defaultValue)
            } else {
                throw exception
            }
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ComplicationsDataSerializer.defaultValue
        )*/

    private val _uiState = MutableStateFlow<SettingsUIState>(SettingsUIState.Initial)
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    override fun onCleared() {
        super.onCleared()
        contextualActions = null
    }

    /***********************************************************************************************
     ***************************************** UI State Methods ************************************
     ***********************************************************************************************/

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = createSettingsList()
                _uiState.value = SettingsUIState.SettingsLoaded(settings)
            } catch (e: Exception) {
                _uiState.value = SettingsUIState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun updateSwitchesSetting(settingId: Int, checked: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is SettingsUIState.SettingsLoaded) return@launch
            val settingToUpdate = currentState.settings.find { it.id == settingId } ?: return@launch
            Log.v(
                TAG,
                "Setting: $settingToUpdate - SettingId: ${settingToUpdate.id}"
            )

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

            if (settingId == 0) handleLocationService(checked)

            updateSettingsUIState(SettingsUIState.SettingsLoaded(updatedSettings))
        }
    }

    private fun updateSettingsUIState(newState: SettingsUIState) {
        _uiState.value = newState
    }

    private fun handleLocationService(checked: Boolean) {
        contextualActions?.toggleLocationService(checked)
    }

    /***********************************************************************************************
     ***************************************** DataStore State Methods *****************************
     ***********************************************************************************************/

    /*fun updateIsMilitaryTime(isMilitaryTime: Boolean) = viewModelScope.launch {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                isMilitaryTime = isMilitaryTime
            )
        }
    }*/

    fun handleFlyMode(checked: Boolean) {
        if (checked) {
            // TODO: Setup the update interval to 2 minutes
        } else {
            // TODO: Setup the update interval to 15 minutes
        }
    }


    fun updateIntervalSetting(time: LocalTime) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState !is SettingsUIState.SettingsLoaded) return@launch

            // TODO: Setup parsing the time into an interval
            // TODO: save the interval to the data store
            // TODO: trigger snack-bar to show the user that the interval has been updated

            Log.v(
                TAG,
                "Selected Option: $time"
            )
        }

    }
}