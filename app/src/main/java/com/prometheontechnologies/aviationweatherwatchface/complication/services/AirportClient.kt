package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.location.Location
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.NearestAirport
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherData
import kotlinx.coroutines.flow.Flow

interface AirportClient {
    fun getAirportUpdates(currentLocation: Location): Flow<Pair<NearestAirport, WeatherData>>
    class AirportNotAvailableException(message: String) : Exception()
}