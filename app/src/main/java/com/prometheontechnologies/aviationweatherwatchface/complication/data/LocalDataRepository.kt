package com.prometheontechnologies.aviationweatherwatchface.complication.data

import com.prometheontechnologies.aviationweatherwatchface.complication.dto.LocationService
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocalDataRepository {
    private val _locationServiceData = MutableStateFlow<LocationService?>(null)
    private val _weatherServiceData = MutableStateFlow<WeatherService?>(null)

    val locationData: StateFlow<LocationService?> = _locationServiceData.asStateFlow()
    val weatherData: StateFlow<WeatherService?> = _weatherServiceData.asStateFlow()

    fun updateLocationData(newLocationData: LocationService?) {
        _locationServiceData.value = newLocationData
    }

    fun clearLocationData() {
        _locationServiceData.value = null
    }

    fun updateWeatherData(newWeatherData: WeatherService?) {
        _weatherServiceData.value = newWeatherData
    }
}