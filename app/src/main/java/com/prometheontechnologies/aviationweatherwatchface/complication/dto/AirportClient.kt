package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface AirportClient {
    fun getAirportUpdates(currentLocation: Location): Flow<Pair<NearestAirport, WeatherData>>
    class AirportNotAvailableException(message: String) : Exception()
}