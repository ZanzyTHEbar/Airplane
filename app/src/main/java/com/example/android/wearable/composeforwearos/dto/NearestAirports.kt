package com.example.android.wearable.composeforwearos.dto

import com.example.android.wearable.composeforwearos.data.Airport

data class NearestAirport(
    val distance: Double,
    val nearestAirport: Airport
)
