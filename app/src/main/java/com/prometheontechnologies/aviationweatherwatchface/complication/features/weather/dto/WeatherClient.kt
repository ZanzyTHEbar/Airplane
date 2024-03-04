package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto

import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.APIModel
import kotlinx.coroutines.flow.Flow

interface WeatherClient {
    fun getWeatherUpdates(ident: String): Flow<WeatherData>
    suspend fun callWeatherApi(ident: String): Result<APIModel>
    class WeatherNotAvailableException(message: String) : Exception(message)
}