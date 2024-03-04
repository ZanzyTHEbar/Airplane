package com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto

import android.location.Location
import kotlinx.coroutines.flow.Flow

data class DefaultLocationClientData(
    val initialLoad: Boolean = false,
    val location: Location
)

/**
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

interface LocationClient {

    fun getLocationUpdates(): Flow<DefaultLocationClientData>

    class LocationNotAvailableException(message: String) : Exception(message)
}