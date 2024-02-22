package com.prometheontechnologies.aviationweatherwatchface.complication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsSettingsStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.UserPreferences
import kotlinx.coroutines.flow.Flow

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "app_data_store.pb"

object DataStoreModule {
    fun provideProtoDataStore(appContext: Context): DataStore<ComplicationsSettingsStore> {
        return DataStoreFactory.create(
            serializer = ComplicationsDataSerializer,
            produceFile = { appContext.dataStoreFile(DATA_STORE_FILE_NAME) },
            corruptionHandler = ReplaceFileCorruptionHandler {
                ComplicationsSettingsStore()
            }
        )
    }
}

/** REPOSITORY **/
class ComplicationsDataRepository private constructor(
    private val dataStore: DataStore<ComplicationsSettingsStore>
) {
    val appData: Flow<ComplicationsSettingsStore> = dataStore.data

    suspend fun updateComplicationData(complicationsDataStore: ComplicationsDataStore) {
        dataStore.updateData { currentData ->
            currentData.copy(complicationsDataStore = complicationsDataStore)
        }
    }

    suspend fun updateUserPreferences(userPreferences: UserPreferences) {
        dataStore.updateData { currentData ->
            currentData.copy(userPreferences = userPreferences)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ComplicationsDataRepository? = null

        fun getInstance(dataStore: DataStore<ComplicationsSettingsStore>): ComplicationsDataRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ComplicationsDataRepository(dataStore).also { INSTANCE = it }
            }
    }
}