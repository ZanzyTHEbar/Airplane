package com.prometheontechnologies.aviationweatherwatchface.complication.features.airport

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface AirportClient {
    fun getAirportUpdates(currentLocation: Location): Flow<NearestAirport>
    class AirportNotAvailableException(message: String) : Exception(message)
}