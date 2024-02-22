package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import com.prometheontechnologies.aviationweatherwatchface.complication.data.Airport

data class NearestAirport(
    val distance: Double = 0.0,
    val nearestAirport: Airport = Airport(
        id = 0,
        ident = "",
        type = "",
        name = "",
        latitudeDeg = 0.0,
        longitudeDeg = 0.0,
        elevationFt = 0.0,
        continent = "",
        isoCountry = "",
        isoRegion = "",
        municipality = "",
        scheduledService = "",
        gpsCode = "",
        iataCode = "",
        localCode = "",
        homeLink = "",
        wikipediaLink = "",
        keywords = ""
    )
)
