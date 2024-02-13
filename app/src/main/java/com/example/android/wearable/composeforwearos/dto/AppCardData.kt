package com.example.android.wearable.composeforwearos.dto

import android.location.Location

data class AppCardData(
    val location: String,
    val temp: Int,
    val windSpeed: Int,
    val windDirection: Int,
    val time: String
)
