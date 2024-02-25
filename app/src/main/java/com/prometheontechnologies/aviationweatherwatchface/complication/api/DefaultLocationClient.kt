package com.prometheontechnologies.aviationweatherwatchface.complication.api

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.DefaultLocationClientData
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.LocationClient
import com.prometheontechnologies.aviationweatherwatchface.complication.hasLocationPermissions
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
    override fun getLocationUpdates(interval: Long): Flow<DefaultLocationClientData> {
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
                    "This hardware doesn't have GPS or it is disabled"
                Log.e(TAG, message)

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                if (!notificationManager.areNotificationsEnabled()) {
                    Toast.makeText(
                        context,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                val notification = Utilities.notificationBuilder(
                    context,
                    context.getString(R.string.location_service_notification_channel_id),
                    "Location not available",
                    "$message enable it or pair with your phone to receive location information.",
                    android.R.drawable.ic_dialog_alert
                )
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setColor(context.getColor(android.R.color.holo_orange_dark))
                    .build()

                notificationManager.notify(101, notification)

                throw LocationClient.LocationNotAvailableException(message)
            }

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                interval
            )
                //.setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(45))
                // set the min update distance to 2 nautical miles
                .setMinUpdateDistanceMeters(3704f)
                .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(2))
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let { location ->
                        launch {
                            val clientData = DefaultLocationClientData(
                                initialLoad = false,
                                location = location
                            )
                            send(clientData)
                        }
                    }
                }
            }

            client
                .lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        launch {
                            val clientData = DefaultLocationClientData(
                                initialLoad = true,
                                location = it
                            )
                            send(clientData)
                        }
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "Error getting last location", it)
                    throw LocationClient.LocationNotAvailableException("Error getting last location")
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