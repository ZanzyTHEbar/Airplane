package com.prometheontechnologies.aviationweatherwatchface.complication.api

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.APIModel
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherApi
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DefaultWeatherClient(private val context: Context) : WeatherClient {

    companion object {
        private val TAG = DefaultWeatherClient::class.java.simpleName
    }

    override suspend fun callWeatherApi(ident: String): Result<APIModel> {
        val weatherDTO: List<APIModel> = WeatherApi.apiInstance.getMetarDetails(
            ident, true, "json"
        )

        if (weatherDTO.isEmpty()) {
            val message =
                "Weather API Client received a null response, trying the next closest airport."

            Log.e(TAG, message)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notification = Utilities.notificationBuilder(
                context,
                context.getString(R.string.location_service_notification_channel_id),
                "Aviation Weather Watchface",
                message,
                android.R.drawable.ic_dialog_info
            )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(false)
                .build()
            notificationManager.notify(103, notification)
            return Result.failure(WeatherClient.WeatherNotAvailableException(message))
        }

        Log.d(TAG, "Weather DTO: $weatherDTO")

        // TODO: Setup logic to handle multiple weather data
        return Result.success(weatherDTO.first())
    }

    override fun getWeatherUpdates(ident: String): Flow<WeatherService> {
        return callbackFlow {
            val result = callWeatherApi(ident)
            if (result.isSuccess) {
                val apiData = result.getOrThrow()
                val data = Result.success(
                    WeatherService(
                        temp = apiData.temp,
                        dewPt = apiData.dewp,
                        windSpeed = apiData.wspd,
                        windDirection = apiData.wdir,
                        sensorTime = apiData.reportTime,
                        visibility = apiData.visib,
                        clouds = apiData.clouds
                    )
                ).getOrThrow()
                trySend(data).isSuccess
            }

            awaitClose {
                Log.d(TAG, "Weather updates flow closed.")
            }
        }
    }
}
