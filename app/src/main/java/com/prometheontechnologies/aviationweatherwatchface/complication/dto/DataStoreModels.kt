package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import android.location.Location
import com.prometheontechnologies.aviationweatherwatchface.complication.data.LocationSerializer
import com.prometheontechnologies.aviationweatherwatchface.complication.services.toText
import kotlinx.serialization.Serializable

@Serializable
data class ComplicationsSettingsStore(
    val userPreferences: UserPreferences = UserPreferences(),
    val locationServiceDataStore: LocationService = LocationService(),
    val weatherServiceDataStore: WeatherService = WeatherService()
)

/** PREFERENCES **/
@Serializable
data class UserPreferences(
    // TIME
    val isMilitaryTime: Boolean = true,
    val isLeadingZeroTime: Boolean = true,

    // WEATHER
    val weatherServiceUpdatePeriod: Long = 15,

    // User
    val notificationEnabled: Boolean = true
)

@Serializable
data class LocationService(
    val ident: String = "",
    @Serializable(with = LocationSerializer::class)
    val location: Location = Location(""),
    val distance: Double = 0.0,
) {
    fun toText(): String {
        return "Ident: $ident\n" +
                "Location: ${location.toText()}\n" +
                "Distance: $distance\n"
    }
}

@Serializable
data class WeatherService(
    val temp: Double = 0.0,
    val dewPt: Double = 0.0,
    val windSpeed: Int = 0,
    val windDirection: Int = 0,
    val sensorTime: String = "",
    val visibility: String = "",
    val clouds: List<Cloud> = emptyList()
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
