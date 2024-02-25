package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
data class ComplicationsSettingsStore(
    val userPreferences: UserPreferences = UserPreferences(),
    val complicationsDataStore: ComplicationsDataStore = ComplicationsDataStore()
)

/** PREFERENCES **/
@Serializable
data class UserPreferences(
    // TIME
    val isMilitaryTime: Boolean = true,
    val isLeadingZeroTime: Boolean = true,

    // LOCATION
    val coarsePermission: Boolean = false,
    val finePermission: Boolean = false,
)

@Serializable
data class ComplicationsDataStore(
    @PrimaryKey
    val ident: String = "",
    val distance: Double = 0.0,
    val temperature: Double = 0.0,
    val dewPoint: Double = 0.0,
    val windSpeed: Int = 0,
    val windDirection: Int = 0,
)
