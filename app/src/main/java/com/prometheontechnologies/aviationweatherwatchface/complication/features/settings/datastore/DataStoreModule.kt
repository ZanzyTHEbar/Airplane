package com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.UserPreferences

private const val DATA_STORE_FILE_NAME = "aviation_weather_prefs.pb"

val Context.complicationsDataStore: DataStore<UserPreferences> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = DataStoreSerializer
)