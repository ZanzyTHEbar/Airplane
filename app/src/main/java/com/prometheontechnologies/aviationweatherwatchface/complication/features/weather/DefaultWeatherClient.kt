package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather

import android.content.Context
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.APIModel
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.WeatherApi
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto.WeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto.WeatherData
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

            // TODO: Setup notifications for weather

            return Result.failure(WeatherClient.WeatherNotAvailableException(message))
        }

        Log.d(TAG, "Weather DTO: $weatherDTO")

        // TODO: Setup logic to handle multiple weather data
        return Result.success(weatherDTO.first())
    }

    override fun getWeatherUpdates(ident: String): Flow<WeatherData> {
        return callbackFlow {
            val result = callWeatherApi(ident)
            if (result.isSuccess) {
                val apiData = result.getOrThrow()
                val data = Result.success(
                    WeatherData(
                        temp = apiData.temp,
                        dewPt = apiData.dewp,
                        windSpeed = apiData.wspd,
                        windDirection = apiData.wdir,
                        sensorTime = apiData.reportTime,
                        visibility = apiData.visib,
                        clouds = apiData.clouds
                    )
                ).getOrThrow()

                Log.d(TAG, "Weather updates flow sent: $data")
                trySend(data).isSuccess
            }

            awaitClose {
                Log.d(TAG, "Weather updates flow closed.")
            }
        }
    }
}
