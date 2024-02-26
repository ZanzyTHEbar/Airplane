package com.prometheontechnologies.aviationweatherwatchface.complication.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prometheontechnologies.aviationweatherwatchface.complication.ManagerViewModel
import com.prometheontechnologies.aviationweatherwatchface.complication.compose.AppManager
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.SettingsContextualActions
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.createSettingsList
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService
import com.prometheontechnologies.aviationweatherwatchface.complication.services.WeatherUpdateWorker
import java.util.concurrent.TimeUnit

class AppManagerActivity : ComponentActivity(), SettingsContextualActions {
    companion object {
        private val TAG = AppManagerActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel: ManagerViewModel by viewModels()
        viewModel.contextualActions = this


        // Start the location service
        Intent(applicationContext, LocationUpdateService::class.java).apply {
            action =
                ServicesInterface.Companion.ActionType.START.toString()
            startForegroundService(this)
        }

        // Start the work manager
        scheduleWeatherUpdates(applicationContext)

        val settingsList = createSettingsList()

        setContent {
            AppManager(context = this, settingsList = settingsList, viewModel = viewModel)
        }
    }

    override fun toggleLocationService() {
        when (LocationUpdateService.isRunning) {
            true -> {
                Intent(applicationContext, LocationUpdateService::class.java).apply {
                    action =
                        ServicesInterface.Companion.ActionType.STOP.toString()
                    startForegroundService(this)
                }
            }

            false -> {
                Intent(applicationContext, LocationUpdateService::class.java).apply {
                    action =
                        ServicesInterface.Companion.ActionType.START.toString()
                    startForegroundService(this)
                }
            }
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

}