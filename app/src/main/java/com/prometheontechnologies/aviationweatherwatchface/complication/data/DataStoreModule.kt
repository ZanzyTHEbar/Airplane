package com.prometheontechnologies.aviationweatherwatchface.complication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsSettingsStore

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "app_data_store.pb"

val Context.complicationsDataStore: DataStore<ComplicationsSettingsStore> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = ComplicationsDataSerializer
)