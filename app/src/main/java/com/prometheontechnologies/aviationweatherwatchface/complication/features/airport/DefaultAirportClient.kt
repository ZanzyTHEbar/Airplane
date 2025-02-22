package com.prometheontechnologies.aviationweatherwatchface.complication.features.airport

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.Airport
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.AirportDAO
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.UserPreferencesRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto.toText
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto.WeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.hasLocationPermissions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// TODO: https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
// Make sure to calculate the bounding box for the location and then query the database

class DefaultAirportClient(
    private val context: Context,
    private val dao: AirportDAO,
    private val weatherClient: WeatherClient,
    private val repository: UserPreferencesRepository
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
    ): Result<NearestAirport> {
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

        val militaryEnabled =
            LocalDataRepository.militaryEnabled.value ?: repository.readUserPreferences()
                .first().enableMilitary

        // filter to remove all airports that are closed, heliport, or have `Army` or `Air Force` or 'Navy' in the name
        val filteredAirports = closestAirports
            .filterNot {
                it.first.type == "heliport"
            }
            .filterNot {

                if (militaryEnabled) {
                    return@filterNot false
                }

                it.first.name.contains("Army") || it.first.name.contains("Air Force") || it.first.name.contains(
                    "Navy"
                )
            }

        if (filteredAirports.isEmpty()) {
            Log.e(TAG, "No airports found.")
            return Result.failure(AirportClient.AirportNotAvailableException("No airports found."))
        }

        Log.d(TAG, "Filtered Airports: $filteredAirports")

        var closestAirport: Pair<Airport, Float> = Pair(
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
            ), 0.0f
        )

        for (airport in filteredAirports) {
            val result = weatherClient.callWeatherApi(airport.first.ident)
            if (result.isSuccess) {
                closestAirport = airport
                break
            }
        }

        if (closestAirport.first.id == 0) {
            Log.e(TAG, "No airports found.")
            return Result.failure(AirportClient.AirportNotAvailableException("No airports found."))
        }

        val airport = closestAirport.first
        // Convert to kilometers
        val distance = closestAirport.second.div(1000.0)

        val nearestAirport = NearestAirport(
            nearestAirport = airport,
            distance = distance,
        )

        return Result.success(nearestAirport)
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

    @SuppressLint("MissingPermission")
    override fun getAirportUpdates(currentLocation: Location): Flow<NearestAirport> {
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
                val data = findNearestAirport(
                    filteredLocations,
                    currentLocation
                ).getOrNull()

                launch {
                    if (data != null) {
                        send(data)
                    }
                }
            }

            awaitClose {
                Log.d(TAG, "Closing flow")
            }
        }
    }
}