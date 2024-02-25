package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import kotlinx.coroutines.flow.Flow

interface WeatherClient {
    fun getWeatherUpdates(ident: String): Flow<WeatherService>
    suspend fun callWeatherApi(ident: String): Result<APIModel>
    class WeatherNotAvailableException(message: String) : Exception()
}