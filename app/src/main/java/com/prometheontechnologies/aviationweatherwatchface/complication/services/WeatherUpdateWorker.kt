package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.api.DefaultWeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.data.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherService
import kotlinx.coroutines.flow.first

class WeatherUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val locationIdent =
            LocalDataRepository.locationData.value?.ident ?: return Result.retry()

        // Initialize your weather client
        val weatherClient = DefaultWeatherClient(applicationContext)

        return try {
            val update = weatherClient.getWeatherUpdates(locationIdent).first()
            updateData(update)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun updateData(weatherService: WeatherService) {
        LocalDataRepository.updateWeatherData(weatherService)
        Utilities.requestComplicationUpdate(applicationContext, true)
    }
}