package com.prometheontechnologies.aviationweatherwatchface.complication.data.database

import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto.LocationData
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.MetarData
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.WeatherAPIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LocalDataRepository {
    private val _locationServiceData = MutableStateFlow<LocationData?>(null)
    private val _weatherServiceData = MutableStateFlow<WeatherAPIModel?>(null)
    private val _metarData = MutableStateFlow<MetarData?>(null)
    private val _updateInterval = MutableStateFlow<Int?>(null)
    private val _militaryEnabled = MutableStateFlow<Boolean?>(null)

    val locationData: StateFlow<LocationData?> = _locationServiceData.asStateFlow()
    val weatherData: StateFlow<WeatherAPIModel?> = _weatherServiceData.asStateFlow()
    val metarData: StateFlow<MetarData?> = _metarData.asStateFlow()
    val updateInterval: StateFlow<Int?> = _updateInterval.asStateFlow()
    val militaryEnabled: StateFlow<Boolean?> = _militaryEnabled.asStateFlow()

    fun updateLocationData(newLocationData: LocationData?) {
        _locationServiceData.value = newLocationData
    }

    fun updateUpdateInterval(interval: Int?) {
        _updateInterval.value = interval
    }

    fun updateMilitaryEnabled(enabled: Boolean?) {
        _militaryEnabled.value = enabled
    }

    fun clearRepoData() {
        _locationServiceData.value = null
        _weatherServiceData.value = null
        _metarData.value = null
        _updateInterval.value = null
        _militaryEnabled.value = null
    }

    fun updateWeatherData(newWeatherData: WeatherAPIModel?) {
        _weatherServiceData.value = newWeatherData
    }

    fun updateMetarData(newMetarData: MetarData?) {
        _metarData.value = newMetarData
    }
}