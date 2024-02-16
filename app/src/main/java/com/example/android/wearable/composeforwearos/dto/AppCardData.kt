package com.example.android.wearable.composeforwearos.dto

data class AppCardData(
    val location: String,
    val temp: Double,
    val windSpeed: Int,
    val windDirection: Int,
    val time: String
)
