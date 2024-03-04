package com.prometheontechnologies.aviationweatherwatchface.complication.features.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    // must always be the first field
    var locationServiceEnabled: Boolean = true,
    // User
    var notificationsEnabled: Boolean = false,
    var flyingMode: Boolean = false,
    // TIME
    var enableMilitary: Boolean = true,
    var isLeadingZeroTime: Boolean = true,
    // WEATHER
    var weatherServiceUpdatePeriod: Long = 15,
    var initialLoad: Boolean = true,
) {
    companion object {
        val defaultValue = UserPreferences()
    }
}
