package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import android.location.Location
import kotlinx.coroutines.flow.Flow

data class DefaultLocationClientData(
    val initialLoad: Boolean = false,
    val location: Location
)

interface LocationClient {
    fun getLocationUpdates(interval: Long): Flow<DefaultLocationClientData>

    class LocationNotAvailableException(message: String) : Exception()
}