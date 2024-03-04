package com.prometheontechnologies.aviationweatherwatchface.complication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.UserPreferencesRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.LocationUpdateService
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingsContextualActions
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.WeatherUpdateWorker
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.MainViewModel
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.UserPreferencesViewModelFactory
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose.MainAppEntry
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity(), SettingsContextualActions {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var repository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = UserPreferencesRepository(applicationContext)
        val factory = UserPreferencesViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        viewModel.contextualActions = this

        setContent {
            MainAppEntry(
                applicationContext,
                viewModel
            )
        }
    }

    override fun scheduleWeatherUpdates(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(15, TimeUnit.MINUTES)
            .addTag("WeatherUpdateWork")
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeatherUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing periodic work, don't replace
            workRequest
        )
    }

    override suspend fun toggleLocationService(toggle: Boolean) {

        val toggleText = if (toggle) "Enabled" else "Disabled"
        val currentLocationEnabled = repository.readUserPreferences().first().locationServiceEnabled

        Log.d(TAG, "Location Service $currentLocationEnabled -> $toggleText")

        val initialLoad = repository.readUserPreferences().first().initialLoad

        Log.d(TAG, "Initial Load: $initialLoad")

        if (initialLoad) {
            Intent(applicationContext, LocationUpdateService::class.java).apply {
                action = ServicesInterface.Companion.ActionType.START.toString()
                startService(this)
            }

            val updatedUserPreferences = repository.readUserPreferences().first().copy(
                locationServiceEnabled = true,
                initialLoad = false
            )
            repository.saveUserPreference(updatedUserPreferences)

            viewModel.updateLocationServicesButton(toggle)

            return
        }

        if (currentLocationEnabled == toggle) return

        val updatedUserPreferences = repository.readUserPreferences().first().copy(
            locationServiceEnabled = toggle
        )

        repository.saveUserPreference(updatedUserPreferences)

        val newLocationEnabled = repository.readUserPreferences().first().locationServiceEnabled

        val actionIntent = if (newLocationEnabled) {
            ServicesInterface.Companion.ActionType.START.toString()
        } else {
            ServicesInterface.Companion.ActionType.STOP.toString()
        }

        Intent(applicationContext, LocationUpdateService::class.java).apply {
            action = actionIntent
            startService(this)
        }

        viewModel.updateLocationServicesButton(newLocationEnabled)
    }
}