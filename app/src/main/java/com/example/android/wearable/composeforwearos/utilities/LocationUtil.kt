package com.example.android.wearable.composeforwearos.utilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.example.android.wearable.composeforwearos.api.APIModel
import com.example.android.wearable.composeforwearos.api.WeatherApi
import com.example.android.wearable.composeforwearos.data.Airport
import com.example.android.wearable.composeforwearos.data.AirportDAO
import com.example.android.wearable.composeforwearos.dto.AppCardData
import com.example.android.wearable.composeforwearos.dto.NearestAirport
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
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

class LocationUtil(
    private val dao: AirportDAO
) {
    val locationDataLoaded = mutableStateOf(false)
    val nearestAirportLoaded = mutableStateOf(false)

    val locationData by lazy {
        mutableStateOf(
            AppCardData(
                location = "", temp = 0.0, windDirection = 0, windSpeed = 0, time = ""
            )
        )
    }

    val nearestAirportData by lazy {
        mutableStateOf(
            NearestAirport(
                nearestAirport = Airport(
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
                ), distance = 0.0
            )
        )
    }

    private val nearbyLocations = mutableListOf<Airport>()
    private val loc1: Location = Location("Current Location")
    private val loc2: Location = Location("Airport Location")

    /**
     * Calculate the distance between two locations using the Haversine formula
     * <p> This uses the Haversine formula to calculate the distance between two locations
     * <p class="note"> The Haversine formula is used to calculate the distance between two points on the Earth's surface given their latitude and longitude.
     * It is important to note that this formula assumes that the Earth is a perfect sphere, which is not the case, but it is accurate enough for most purposes.
     */
    private fun distance(): Double {
        val dLat = Math.toRadians(loc2.latitude - loc1.latitude)  // deg2rad below
        val dLon = Math.toRadians(loc2.longitude - loc1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(loc1.latitude)) * cos(
            Math.toRadians(loc2.latitude)
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

    private fun findNearbyLocations(
        potentialLocations: List<Airport>,
        rangeKm: Double = 200.0,
    ) {
        val locations = potentialLocations.filter { airport ->
            //Log.v(TAG, "Potential Locations: $airport")
            loc2.latitude = airport.latitudeDeg
            loc2.longitude = airport.longitudeDeg
            distance() <= rangeKm

        }

        nearbyLocations.clear()
        nearbyLocations.addAll(locations)

        /*Log.d(TAG, "Nearby Locations: $nearbyLocations.")
        Log.d(
            TAG,
            "Nearby Locations Size: ${nearbyLocations.size} - Potential Locations Size: ${potentialLocations.size}"
        )*/
    }

    fun getNearbyLocation() {
        Log.d(TAG, "Located Airports: ${this.nearbyLocations}")

        val closestAirports = this.nearbyLocations.map {
            val lat = it.latitudeDeg
            val lon = it.longitudeDeg
            val airPortLocation = Location("Current Location").apply {
                latitude = lat
                longitude = lon
            }
            val distance = airPortLocation.distanceTo(loc1)

            it to distance
        }.sortedBy {
            // Sort by smallest distance first
            it.second
        }
        // TODO setup military filter to only be enabled if the setting is enabled

        // filter to remove all airports that are closed, heliport, or have `Army` or `Air Force` or 'Navy' in the name
        val filteredAirports = closestAirports.filterNot {
            it.first.type == "heliport"
        }.filterNot {
            it.first.name.contains("Army") || it.first.name.contains("Air Force") || it.first.name.contains(
                "Navy"
            )
        }

        if (filteredAirports.isEmpty()) {
            Log.e(TAG, "No airports found.")
            return
        }

        Log.d(TAG, "Filtered Airports: $filteredAirports")

        val closestAirport = filteredAirports.first()
        val airport = closestAirport.first
        val distance = closestAirport.second

        nearestAirportLoaded.value = true
        nearestAirportData.value = NearestAirport(
            nearestAirport = airport, distance = distance.toDouble()
        )
    }

    private fun dbQuery(rangeKm: Double = 200.0): Pair<Pair<Double, Double>, Pair<Double, Double>> {
        // Latitude range remains constant because 1 degree of latitude is approximately 111 kilometers everywhere.
        val latRange = rangeKm / 111.0
        // Calculate longitude range dynamically based on user's latitude.
        val lonRange = calculateLongitudeRangeAtLatitude(loc1.latitude, rangeKm)
        val minLat = loc1.latitude - latRange
        val maxLat = loc1.latitude + latRange
        val minLon = loc1.longitude - lonRange
        val maxLon = loc1.longitude + lonRange
        return Pair(Pair(minLat, maxLat), Pair(minLon, maxLon))
    }

    fun createLocationRequest(
        context: Context, fusedLocationClient: FusedLocationProviderClient
    ) {
        //.setIntervalMillis(TimeUnit.SECONDS.toMillis(10))
        val locationRequest = LocationRequest.Builder(
            TimeUnit.SECONDS.toMillis(30)
        )
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
            .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(5)).build()

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                //super.onLocationResult(p0)
                val location = Location("")
                location.set(p0.locations[p0.locations.size - 1])
                Log.d(TAG, "Location: ${location.toText()}")

                CoroutineScope(Dispatchers.IO).launch {
                    if (!kotlin.coroutines.coroutineContext.isActive) {
                        Log.wtf(TAG, "Coroutine is not active, or was cancelled.")
                        return@launch
                    }

                    // Set the current location
                    loc1.set(location)

                    // Get the nearby locations to generate the ranged query
                    val (latRange, lonRange) = dbQuery()

                    // Get the airports within the range from the database
                    dao.getAirportsByLocationsInRangeStartingWithK(
                        latRange.first, latRange.second, lonRange.first, lonRange.second
                    ).collect { airports ->
                        findNearbyLocations(airports)
                        Log.d(TAG, "Airports from DB: $airports")
                        getNearbyLocation()

                        val weatherDTO: List<APIModel>

                        nearestAirportData.value.nearestAirport.let {
                            // Check if nearestAirportData.value.nearestAirport is not null
                            if (it.ident.isEmpty()) {
                                Log.e(TAG, "Nearest Airport is null.")
                                return@collect
                            }

                            weatherDTO = WeatherApi.apiInstance.getMetarDetails(
                                it.ident, true, "json"
                            )

                            /*if (it.type == "small_airport") {
                                Log.d(
                                    TAG,
                                    "Small Airports are not currently supported: ${it.ident}"
                                )
                                TODO()
                                *//*val lat = it.latitudeDeg
                                val lon = it.longitudeDeg

                                // Create bounding box around nearestLocationData
                                // Latitude range remains constant because 1 degree of latitude is approximately 111 kilometers everywhere.
                                val mLatRange = 200.0 / 111.0
                                // Calculate longitude range dynamically based on user's latitude.
                                val mLonRange = calculateLongitudeRangeAtLatitude(lat, 200.0)
                                val minLat = lat - mLatRange
                                val maxLat = lat + mLatRange
                                val minLon = lon - mLonRange
                                val maxLon = lon + mLonRange

                                weatherDTO = WeatherApi
                                    .apiInstance
                                    .getMetarDetailsBBOX(
                                        true,
                                        "json",
                                        "${minLat},${minLon},${maxLat},${maxLon}"
                                    )*//*
                            } else {

                            }*/
                        }

                        if (weatherDTO.isEmpty()) {
                            Log.e(TAG, "Weather DTO is empty.")
                            return@collect
                        }

                        val apiData: APIModel = weatherDTO[0]

                        Log.d(TAG, "Weather DTO: $apiData")

                        locationDataLoaded.value = true
                        locationData.value = AppCardData(
                            location = location.toText(),
                            temp = apiData.temp,
                            windDirection = apiData.wdir,
                            windSpeed = apiData.wspd,
                            time = apiData.receiptTime
                        )
                    }
                }
            }
        }, Looper.getMainLooper())
    }

    fun stopLocationUpdates(fusedLocationClient: FusedLocationProviderClient) {
        fusedLocationClient.removeLocationUpdates(object : LocationCallback() {})
    }

    companion object {
        private const val EARTH_RADIUS = 6371.0 // Radius of the earth in km
        private val TAG = LocationUtil::class.java.simpleName
    }
}