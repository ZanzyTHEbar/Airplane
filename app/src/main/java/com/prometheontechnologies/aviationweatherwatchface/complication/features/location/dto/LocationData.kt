package com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto

import android.location.Location
import kotlinx.serialization.Serializable

@Serializable
data class LocationData(
    val ident: String = "",
    @Serializable(with = LocationSerializer::class)
    val location: Location = Location(""),
    val distance: Double = 0.0,
) {
    fun toText(): String {
        return "Ident: $ident\n" +
                "Location: ${location.toText()}\n" +
                "Distance: $distance\n"
    }
}
