package com.prometheontechnologies.aviationweatherwatchface.complication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prometheontechnologies.aviationweatherwatchface.complication.data.ComplicationsDataSerializer
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsSettingsStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.SettingsContextualActions
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException

class ManagerViewModel(context: Context) : ViewModel() {
    private val dataStore: DataStore<ComplicationsSettingsStore> = context.complicationsDataStore

    var contextualActions: SettingsContextualActions? = null

    override fun onCleared() {
        super.onCleared()
        contextualActions = null
    }

    val complicationsSettings: StateFlow<ComplicationsSettingsStore> = dataStore.data
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
        )

    fun updateIsMilitaryTime(isMilitaryTime: Boolean) = viewModelScope.launch {
        dataStore.updateData { currentSettings ->
            currentSettings.copy(
                userPreferences = currentSettings.userPreferences.copy(
                    isMilitaryTime = isMilitaryTime
                )
            )
        }
    }

    fun handleLocationService() {
        contextualActions?.toggleLocationService()
    }

}