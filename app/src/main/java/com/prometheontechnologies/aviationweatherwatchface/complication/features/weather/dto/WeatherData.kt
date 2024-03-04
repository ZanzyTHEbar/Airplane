package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto

import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.Cloud
import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val temp: Double = 0.0,
    val dewPt: Double = 0.0,
    val windSpeed: Int = 0,
    val windDirection: Double = 0.0,
    val sensorTime: String = "",
    val visibility: String = "",
    val clouds: List<Cloud> = emptyList(),
    val pressure: Double = 0.0,
    val densityAltitude: Double = 0.0,
    val freezingLayer: Int = 0,
    val cloudLayer: Int = 0,
) {
    fun toText(): String {
        return "Temperature: $temp\n" +
                "Dew Point: $dewPt\n" +
                "Wind Speed: $windSpeed\n" +
                "Wind Direction: $windDirection\n" +
                "Time: $sensorTime\n" +
                "Visibility: $visibility\n" +
                "Cloud: $clouds\n"
    }
}
