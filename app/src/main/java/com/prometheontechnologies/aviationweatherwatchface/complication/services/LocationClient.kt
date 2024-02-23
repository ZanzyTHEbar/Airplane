package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<Location>

    class LocationNotAvailableException(message: String) : Exception()

}