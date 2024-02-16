package com.example.android.wearable.composeforwearos.data

import com.example.android.wearable.composeforwearos.dto.AppCardData

data class AirportsState(
    val nearestAirport: Pair<Airport, AppCardData> = Pair(
        Airport(
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
        ),
        AppCardData(
            location = "",
            temp = 0.0,
            windDirection = 0,
            windSpeed = 0,
            time = ""
        )
    ),
    val closestAirports: List<Airport> = emptyList(),
    val distance: Double = 0.0,
    val loading: Boolean = false,
    val msg: String = "",
    //val sortType: SortType = SortType.LOCATION
)
