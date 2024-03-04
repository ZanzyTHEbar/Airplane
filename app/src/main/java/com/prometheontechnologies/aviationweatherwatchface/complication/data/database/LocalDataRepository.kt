package com.prometheontechnologies.aviationweatherwatchface.complication.data.database

import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto.LocationData
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto.WeatherData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocalDataRepository {
    private val _locationServiceData = MutableStateFlow<LocationData?>(null)
    private val _weatherServiceData = MutableStateFlow<WeatherData?>(null)

    val locationData: StateFlow<LocationData?> = _locationServiceData.asStateFlow()
    val weatherData: StateFlow<WeatherData?> = _weatherServiceData.asStateFlow()

    fun updateLocationData(newLocationData: LocationData?) {
        _locationServiceData.value = newLocationData
    }

    fun clearLocationData() {
        _locationServiceData.value = null
    }

    fun updateWeatherData(newWeatherData: WeatherData?) {
        _weatherServiceData.value = newWeatherData
    }
}