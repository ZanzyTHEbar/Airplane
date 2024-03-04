package com.prometheontechnologies.aviationweatherwatchface.complication.features.settings

import android.content.Context

interface SettingsContextualActions {
    suspend fun toggleLocationService(toggle: Boolean)
    fun scheduleWeatherUpdates(context: Context)
    //val dataStore: DataStore<UserPreferences>
}