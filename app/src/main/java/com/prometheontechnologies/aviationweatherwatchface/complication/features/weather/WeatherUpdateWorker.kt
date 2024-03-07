package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.WeatherAPIModel
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.requestComplicationUpdate
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

    private fun updateData(weatherService: WeatherAPIModel) {
        LocalDataRepository.updateWeatherData(weatherService)
        applicationContext.requestComplicationUpdate(true)
    }
}