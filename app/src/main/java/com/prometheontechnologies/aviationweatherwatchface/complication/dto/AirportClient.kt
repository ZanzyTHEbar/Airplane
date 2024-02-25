package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface AirportClient {
    fun getAirportUpdates(currentLocation: Location): Flow<NearestAirport>
    class AirportNotAvailableException(message: String) : Exception()
}