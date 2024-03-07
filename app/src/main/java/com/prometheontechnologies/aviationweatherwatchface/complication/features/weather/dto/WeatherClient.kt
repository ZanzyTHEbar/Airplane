package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto

import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.WeatherAPIModel
import kotlinx.coroutines.flow.Flow

interface WeatherClient {
    fun getWeatherUpdates(ident: String): Flow<WeatherAPIModel>
    suspend fun callWeatherApi(ident: String): Result<WeatherAPIModel>
    class WeatherNotAvailableException(message: String) : Exception(message)
}