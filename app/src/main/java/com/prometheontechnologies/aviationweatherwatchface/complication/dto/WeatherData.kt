package com.prometheontechnologies.aviationweatherwatchface.complication.dto

data class WeatherData(
    val location: String = "",
    val temp: Double = 0.0,
    val dewPt: Double = 0.0,
    val windSpeed: Int = 0,
    val windDirection: Int = 0,
    val time: String = ""
) {
    fun toText(): String {
        return "Location: $location\n" +
                "Temperature: $temp\n" +
                "Dew Point: $dewPt\n" +
                "Wind Speed: $windSpeed\n" +
                "Wind Direction: $windDirection\n" +
                "Time: $time"
    }
}

