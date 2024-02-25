package com.prometheontechnologies.aviationweatherwatchface.complication.api

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.data.Airport
import com.prometheontechnologies.aviationweatherwatchface.complication.data.AirportDAO
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.APIModel
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.AirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.NearestAirport
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherData
import com.prometheontechnologies.aviationweatherwatchface.complication.hasLocationPermissions
import com.prometheontechnologies.aviationweatherwatchface.complication.services.WeatherApi
import com.prometheontechnologies.aviationweatherwatchface.complication.services.toText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// TODO: https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
// Make sure to calculate the bounding box for the location and then query the database

class DefaultAirportClient(
    private val context: Context,
    private val dao: AirportDAO
) : AirportClient {

    companion object {
        private const val EARTH_RADIUS = 6371.0 // Radius of the earth in km
        private val TAG = DefaultAirportClient::class.java.simpleName
    }

    /**
     * Calculate the distance between two locations using the Haversine formula
     * <p> This uses the Haversine formula to calculate the distance between two locations
     * <p class="note"> The Haversine formula is used to calculate the distance between two points on the Earth's surface given their latitude and longitude.
     * It is important to note that this formula assumes that the Earth is a perfect sphere, which is not the case, but it is accurate enough for most purposes.
     */
    private fun distance(
        airportLocation: Location,
        currentLocation: Location
    ): Double {
        val dLat =
            Math.toRadians(airportLocation.latitude - currentLocation.latitude)  // deg2rad below
        val dLon = Math.toRadians(airportLocation.longitude - currentLocation.longitude)
        val a =
            sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(currentLocation.latitude)) * cos(
                Math.toRadians(airportLocation.latitude)
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
        airportLocation: Location,
        currentLocation: Location
    ): List<Airport> {
        return potentialLocations.filter { airport ->
            airportLocation.latitude = airport.latitudeDeg
            airportLocation.longitude = airport.longitudeDeg
            distance(
                airportLocation,
                currentLocation
            ) <= rangeKm
        }
    }

    private suspend fun findNearestAirport(
        nearbyLocations: List<Airport>,
        currentLocation: Location
    ): Pair<NearestAirport?, WeatherData?> {
        Log.d(TAG, "Located Airports: $nearbyLocations")

        val closestAirports = nearbyLocations.map {
            val lat = it.latitudeDeg
            val lon = it.longitudeDeg
            val mAirportLocation = Location("Current Location").apply {
                latitude = lat
                longitude = lon
            }
            val distance = mAirportLocation.distanceTo(currentLocation)
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
                null,
                null
            )
        }

        Log.d(TAG, "Filtered Airports: $filteredAirports")

        var apiData: APIModel? = null
        var closestAirport: Pair<Airport, Float>? = null

        // Setup a recursive call to handle the api, pass in the first airport in the list and if it returns an empty body, call the next airport in the list
        for (airport in filteredAirports) {
            val result = handleApi(airport.first)
            if (result.isSuccess) {
                apiData = result.getOrNull()
                closestAirport = airport
                break
            }
        }

        val airport = closestAirport?.first
        // Convert to kilometers
        val distance = (((closestAirport?.second?.div(1000.0)) ?: 0.0))

        val nearestAirport = airport?.let {
            NearestAirport(
                nearestAirport = it,
                distance = distance,
            )
        }

        val weatherData = apiData?.let {
            WeatherData(
                location = currentLocation.toText(),
                temp = it.temp,
                dewPt = apiData.dewp,
                windSpeed = apiData.wspd,
                windDirection = apiData.wdir,
                time = apiData.reportTime
            )
        }

        return Pair(nearestAirport, weatherData)
    }

    private fun dbQuery(
        rangeKm: Double = 200.0,
        currentLocation: Location
    ): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        // Latitude range remains constant because 1 degree of latitude is approximately 111 kilometers everywhere.
        val latRange = rangeKm / 111.0
        // Calculate longitude range dynamically based on user's latitude.

        val lonRange = calculateLongitudeRangeAtLatitude(currentLocation.latitude, rangeKm)
        val minLat = currentLocation.latitude - latRange
        val maxLat = currentLocation.latitude + latRange
        val minLon = currentLocation.longitude - lonRange
        val maxLon = currentLocation.longitude + lonRange
        return Pair(Pair(minLat, maxLat), Pair(minLon, maxLon))
    }

    private suspend fun handleApi(nearestAirport: Airport): Result<APIModel> {
        val weatherDTO: List<APIModel> = WeatherApi.apiInstance.getMetarDetails(
            nearestAirport.ident, true, "json"
        )

        // TODO setup notifications for when the weather data is not available
        if (weatherDTO.isEmpty()) {
            Log.e(TAG, "Weather DTO is empty.")
            return Result.failure(Exception("Weather DTO is empty."))
        }

        Log.d(TAG, "Weather DTO: $weatherDTO")
        // TODO Setup logic to handle multiple weather data
        return Result.success(weatherDTO.first())
    }

    @SuppressLint("MissingPermission")
    override fun getAirportUpdates(currentLocation: Location): Flow<Pair<NearestAirport, WeatherData>> {
        return callbackFlow {
            if (!context.hasLocationPermissions()) {
                throw AirportClient.AirportNotAvailableException("Location permissions not granted")
            }

            val (latRange, lonRange) = dbQuery(currentLocation = currentLocation)

            dao.getAirportsByLocationsInRangeStartingWithK(
                latRange.first,
                latRange.second,
                lonRange.first,
                lonRange.second
            ).collect { airports ->

                Log.d(TAG, "Current Location: ${currentLocation.toText()}")

                val airportLocation = Location("Airport Location")

                Log.v(TAG, "Getting airports in range from DB")
                Log.d(TAG, "Airports from DB: $airports")
                // Filter the locations by the Haversine formula
                val filteredLocations = filterLocationsByHaversine(
                    potentialLocations = airports,
                    airportLocation = airportLocation,
                    currentLocation = currentLocation
                )
                // Find the nearest airport
                val (nearestAirport, weatherData) = findNearestAirport(
                    filteredLocations,
                    currentLocation
                )

                if (nearestAirport == null || weatherData == null) {
                    throw AirportClient.AirportNotAvailableException("No airports found")
                }

                val data: Pair<NearestAirport, WeatherData> = Pair(nearestAirport, weatherData)

                launch { send(data) }
            }

            awaitClose {}
        }
    }
}