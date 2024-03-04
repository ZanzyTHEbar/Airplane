package com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto

import java.util.concurrent.TimeUnit

data class LocationSettings(
    val interval: Long = TimeUnit.SECONDS.toMillis(45),
    val fastestInterval: Long = TimeUnit.SECONDS.toMillis(45),
    val priority: Int = 100
)
