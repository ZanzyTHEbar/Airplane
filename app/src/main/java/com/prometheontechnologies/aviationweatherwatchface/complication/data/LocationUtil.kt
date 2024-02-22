package com.prometheontechnologies.aviationweatherwatchface.complication.data

import android.location.Location
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.api.APIModel
import com.prometheontechnologies.aviationweatherwatchface.complication.api.WeatherApi
import com.prometheontechnologies.aviationweatherwatchface.complication.complications.TempComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.NearestAirport
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// TODO: https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
// Make sure to calculate the bounding box for the location and then query the database

/**
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

class LocationUtil(private val dao: AirportDAO) {

    var nearestAirportLoaded = false
    var weatherDataLoaded = false
    var weatherData = WeatherData()
    var nearestAirportData = NearestAirport()
    var currentLocation: Location? = null

    private var airportLocation: Location? = null

    companion object {
        private const val EARTH_RADIUS = 6371.0 // Radius of the earth in km
        private val TAG = TempComplicationService::class.java.simpleName
    }

    /**
     * Calculate the distance between two locations using the Haversine formula
     * <p> This uses the Haversine formula to calculate the distance between two locations
     * <p class="note"> The Haversine formula is used to calculate the distance between two points on the Earth's surface given their latitude and longitude.
     * It is important to note that this formula assumes that the Earth is a perfect sphere, which is not the case, but it is accurate enough for most purposes.
     */
    private fun distance(): Double {

        if (currentLocation == null || airportLocation == null) {
            Log.e(TAG, "Current Location or Airport Location is null.")
            return 0.0
        }

        val dLat =
            Math.toRadians(airportLocation!!.latitude - currentLocation!!.latitude)  // deg2rad below
        val dLon = Math.toRadians(airportLocation!!.longitude - currentLocation!!.longitude)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(currentLocation!!.latitude)) * cos(
                Math.toRadians(airportLocation!!.latitude)
            ) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS * c
    }

    /**
     * Width in kilometers=111.32×cos(latitude)
     *
     * <p class="note">Where latitude is in radians. This formula gives you the approximate width in kilometers of
     * one degree of longitude at the specified latitude. You can use this to adjust your longitude
     * range based on how wide a degree of longitude is at the user's current latitude.
     */
    private fun calculateLongitudeRangeAtLatitude(
        latitude: Double, desiredRangeKm: Double
    ): Double {
        val radians = Math.toRadians(latitude)
        val widthPerDegree = cos(radians) * EARTH_RADIUS * (Math.PI / 180)
        return desiredRangeKm / widthPerDegree
    }

    private fun filterLocationsByHaversine(
        potentialLocations: List<Airport>,
        rangeKm: Double = 200.0,
    ): List<Airport> {
        if (airportLocation == null) {
            Log.e(TAG, "Current Location is null.")
            return emptyList()
        }

        return potentialLocations.filter { airport ->
            airportLocation!!.latitude = airport.latitudeDeg
            airportLocation!!.longitude = airport.longitudeDeg
            distance() <= rangeKm
        }
    }

    private fun findNearestAirport(nearbyLocations: List<Airport>): Pair<Airport, Double> {
        Log.d(TAG, "Located Airports: $nearbyLocations")

        val closestAirports = nearbyLocations.map {
            val lat = it.latitudeDeg
            val lon = it.longitudeDeg
            val mAirportLocation = Location("Current Location").apply {
                latitude = lat
                longitude = lon
            }
            val distance = mAirportLocation.distanceTo(currentLocation!!)

            it to distance
        }.sortedBy {
            // Sort by smallest distance first
            it.second
        }
        // TODO setup military filter to only be enabled if the setting is enabled

        // filter to remove all airports that are closed, heliport, or have `Army` or `Air Force` or 'Navy' in the name
        val filteredAirports = closestAirports
            .filterNot {
                it.first.type == "heliport"
            }
            .filterNot {
                it.first.name.contains("Army") || it.first.name.contains("Air Force") || it.first.name.contains(
                    "Navy"
                )
            }

        if (filteredAirports.isEmpty()) {
            Log.e(TAG, "No airports found.")
            return Pair(
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
                ), 0.0
            )
        }

        Log.d(TAG, "Filtered Airports: $filteredAirports")

        val closestAirport = filteredAirports.first()
        val airport = closestAirport.first
        val distance = closestAirport.second / 1000.0 // Convert to kilometers
        return Pair(airport, distance)
    }

    private fun dbQuery(rangeKm: Double = 200.0): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        // Latitude range remains constant because 1 degree of latitude is approximately 111 kilometers everywhere.
        val latRange = rangeKm / 111.0
        // Calculate longitude range dynamically based on user's latitude.

        if (currentLocation == null) {
            Log.e(TAG, "Current Location is null.")
            return Pair(Pair(0.0, 0.0), Pair(0.0, 0.0))
        }

        val lonRange = calculateLongitudeRangeAtLatitude(currentLocation!!.latitude, rangeKm)
        val minLat = currentLocation!!.latitude - latRange
        val maxLat = currentLocation!!.latitude + latRange
        val minLon = currentLocation!!.longitude - lonRange
        val maxLon = currentLocation!!.longitude + lonRange
        return Pair(Pair(minLat, maxLat), Pair(minLon, maxLon))
    }

    private suspend fun handleApi(): Result<APIModel> {
        var weatherDTODeferred: Deferred<List<APIModel>>

        nearestAirportData.nearestAirport.let {
            // Check if nearestAirportData.value.nearestAirport is not null
            if (it.ident.isEmpty()) {
                Log.e(TAG, "Nearest Airport is null.")
                return Result.failure(Exception("Nearest Airport is null."))
            }

            // Handle the API call in parallel with the coroutineScope
            coroutineScope {
                val mAPIResult = async(Dispatchers.IO) {
                    WeatherApi.apiInstance.getMetarDetails(
                        it.ident, true, "json"
                    )
                }
                weatherDTODeferred = mAPIResult
            }
        }

        val weatherDTO = weatherDTODeferred.await()

        // TODO setup notifications for when the weather data is not available
        if (weatherDTO.isEmpty()) {
            Log.e(TAG, "Weather DTO is empty.")
            return Result.failure(Exception("Weather DTO is empty."))
        }

        Log.d(TAG, "Weather DTO: $weatherDTO")
        // TODO Setup logic to handle multiple weather data
        return Result.success(weatherDTO.first())
    }

    private suspend fun getAirportsInRange(): Result<List<Airport>> {
        val (latRange, lonRange) = dbQuery()
        val nearbyLocations = mutableListOf<Airport>()

        dao.getAirportsByLocationsInRangeStartingWithK(
            latRange.first,
            latRange.second,
            lonRange.first,
            lonRange.second
        ).collect { airports ->
            nearbyLocations.addAll(airports)
        }
        return Result.success(nearbyLocations)
    }

    suspend fun handleAirportCoroutine() {
        if (currentLocation == null) {
            Log.e(TAG, "Current Location is null.")
            return
        }

        // Get the airports from the database
        val nearbyLocations = mutableListOf<Airport>()
        coroutineScope {
            val airportsInRange = async { getAirportsInRange() }
            nearbyLocations.addAll(airportsInRange.await().getOrThrow())
        }

        Log.d(TAG, "Airports from DB: $nearbyLocations")

        // Filter the locations by the Haversine formula
        val filteredLocations = filterLocationsByHaversine(nearbyLocations)

        // Find the nearest airport
        val closestAirport = findNearestAirport(filteredLocations)

        // Get the weather data from the API for the nearest airport
        val apiData = handleApi().getOrElse {
            Log.e(TAG, "API Data is empty or malformed.")
            return
        }

        // Update the complications with the new data
        nearestAirportData = NearestAirport(
            nearestAirport = closestAirport.first, distance = closestAirport.second
        )

        weatherData = WeatherData(
            location = currentLocation.toText(),
            temp = apiData.temp,
            dewPt = apiData.dewp,
            windDirection = apiData.wdir,
            windSpeed = apiData.wspd,
            time = apiData.receiptTime
        )

        nearestAirportLoaded = true
        weatherDataLoaded = true
    }
}