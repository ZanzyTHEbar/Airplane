package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    companion object {
        private val TAG = DefaultLocationClient::class.java.simpleName
    }

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if (!context.hasLocationPermissions()) {
                throw LocationClient.LocationNotAvailableException("Location permissions not granted")
            }

            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val isGpsEnabled =
                locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val isNetworkEnabled =
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                val message =
                    "This hardware doesn't have GPS or it is disabled, enable it or pair with your phone to receive location information."
                Log.e(TAG, message)
                val toast = Toast.makeText(
                    context,
                    message,
                    Toast.LENGTH_LONG
                )
                toast.show()
                throw LocationClient.LocationNotAvailableException(message)
            }

            // TODO: Set to PRIORITY_BALANCED_POWER_ACCURACY for production and 5 minutes for interval
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                interval
            )
                //.setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(1))
                .setMaxUpdateDelayMillis(TimeUnit.SECONDS.toMillis(30))
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let { location ->
                        launch { send(location) }
                    }
                }
            }

            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            awaitClose {
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}