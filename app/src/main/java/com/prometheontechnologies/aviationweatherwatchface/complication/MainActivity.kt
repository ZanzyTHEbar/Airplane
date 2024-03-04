package com.prometheontechnologies.aviationweatherwatchface.complication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.LocationUpdateService
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingsContextualActions
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.WeatherUpdateWorker
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.MainViewModel
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose.MainAppEntry
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity(), SettingsContextualActions {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private val viewModel: MainViewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.contextualActions = this

        setContent {
            MainAppEntry(applicationContext, viewModel, ::scheduleWeatherUpdates)
        }
    }

    private fun scheduleWeatherUpdates(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(15, TimeUnit.MINUTES)
            .addTag("WeatherUpdateWork")
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WeatherUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP, // Keep the existing periodic work, don't replace
            workRequest
        )
    }

    override fun toggleLocationService(toggle: Boolean) {
        when (toggle) {
            true -> {
                if (LocationUpdateService.isRunning) {
                    Toast.makeText(
                        applicationContext,
                        "Location Service Already Running",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }

                Intent(applicationContext, LocationUpdateService::class.java).apply {
                    action =
                        ServicesInterface.Companion.ActionType.START.toString()
                    startForegroundService(this)
                }
            }

            false -> {
                if (!LocationUpdateService.isRunning) {
                    Toast.makeText(
                        applicationContext,
                        "Location Service is Not Running",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    return
                }

                Intent(applicationContext, LocationUpdateService::class.java).apply {
                    action =
                        ServicesInterface.Companion.ActionType.STOP.toString()
                    startForegroundService(this)
                }
            }
        }

        val toggleText = if (toggle) "Enabled" else "Disabled"
        Toast.makeText(
            applicationContext,
            "Location Service: $toggleText",
            Toast.LENGTH_SHORT
        )
            .show()
    }
}