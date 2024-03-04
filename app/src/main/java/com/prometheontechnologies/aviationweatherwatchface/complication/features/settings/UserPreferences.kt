package com.prometheontechnologies.aviationweatherwatchface.complication.features.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    // must always be the first field
    val locationServiceEnabled: Boolean = true,
    // User
    val notificationsEnabled: Boolean = false,
    val flyingMode: Boolean = false,
    // TIME
    val enableMilitary: Boolean = true,
    val isLeadingZeroTime: Boolean = true,
    // WEATHER
    val weatherServiceUpdatePeriod: Long = 15
)
