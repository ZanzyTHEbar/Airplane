package com.prometheontechnologies.aviationweatherwatchface.complication.data.database

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.DataStoreSerializer
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

object DataStoreManager {
    private const val DATA_STORE_FILE_NAME = "aviation_weather_prefs.pb"
    private var INSTANCE: DataStore<UserPreferences>? = null

    // Synchronized method to get the singleton instance of DataStore
    @Synchronized
    fun getInstance(context: Context): DataStore<UserPreferences> {
        // Return the existing instance if it's already initialized
        return INSTANCE ?: synchronized(this) {
            // Double-check if it's still null after acquiring the lock
            INSTANCE ?: createDataStore(context).also { INSTANCE = it }
        }
    }

    // Private method to create a new instance of DataStore
    private fun createDataStore(context: Context): DataStore<UserPreferences> {
        return DataStoreFactory.create(
            serializer = DataStoreSerializer,
            produceFile = { context.dataStoreFile(DATA_STORE_FILE_NAME) }
        )
    }

    suspend fun saveUserPreference(context: Context, userPreference: UserPreferences) {
        val dataStore = getInstance(context)
        dataStore.updateData { userPreference }
    }

    fun readUserPreferences(context: Context): Flow<UserPreferences> {
        val dataStore = getInstance(context)
        return dataStore.data.catch { exception ->
            // Handle exception, perhaps by emitting a default value
            if (exception is IOException) {
                Log.e("DataStoreManager", "Error reading user preferences", exception)
                emit(UserPreferences.defaultValue)
            } else {
                throw exception
            }
        }
    }
}

class UserPreferencesRepository(private val context: Context) {

    suspend fun saveUserPreference(userPreference: UserPreferences) {
        DataStoreManager.saveUserPreference(context, userPreference)
    }

    fun readUserPreferences(): Flow<UserPreferences> = DataStoreManager.readUserPreferences(context)
}
