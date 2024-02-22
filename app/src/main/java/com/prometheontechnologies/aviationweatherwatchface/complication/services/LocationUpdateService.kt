package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import java.util.concurrent.TimeUnit

class LocationUpdateService : Service() {

    enum class ActionType {
        START,
        STOP
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var serviceMessenger: Messenger? = null
    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
            isBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            serviceMessenger = null
            isBound = false
        }
    }

    private fun bindToAirportService() {
        Intent(this, AirportService::class.java).also { intent ->
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbindFromAirportService() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }

    private fun sendLocationUpdate(location: Location) {
        Log.d(TAG, "Location update received: $location")
        Log.v(TAG, "isBound: $isBound")
        if (!isBound) return
        // Create a new Bundle to hold location data
        val bundle = Bundle().apply {
            putDouble("latitude", location.latitude)
            putDouble("longitude", location.longitude)
        }

        // Obtain a message and set its what value to the location message identifier
        val msg = Message.obtain(null, AirportService.IncomingHandler.LOCATION_MESSAGE).apply {
            data = bundle // Attach the bundle as the message's data
        }

        try {
            serviceMessenger?.send(msg)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error sending location message to AirportService.", e)
        }
    }

    companion object {
        private val TAG = LocationUpdateService::class.java.simpleName
        private fun hasGps(
            packageManager: PackageManager
        ): Boolean =
            packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ActionType.START.toString() -> {
                Log.d(TAG, "LocationUpdateService started.")
                // Init the location client
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                if (Utilities.checkPermissions(this)) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener {
                            sendLocationUpdate(it)
                        }
                }
                bindToAirportService()
                startLocationUpdates()
            }

            ActionType.STOP.toString() -> {
                Log.d(TAG, "LocationUpdateService stopped.")
                stopLocationUpdates()
                stopSelf()
            }

            else -> {
                Log.e(TAG, "Unknown action.")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {

        /*val notification = Utilities.notificationBuilder(
            this,
            resources.getString(ResourcesR.string.location_service_notification_channel_id),
            "Location Service",
            "Location updates are active.",
            android.R.drawable.ic_menu_mylocation
        ).build()*/

        //startForeground(1, notification)

        // Create the location request to start receiving updates
        // TODO: Set to PRIORITY_BALANCED_POWER_ACCURACY for production and 5 minutes for interval
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TimeUnit.MINUTES.toMillis(1)
        )
            //.setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(TimeUnit.SECONDS.toMillis(30))
            .setMaxUpdateDelayMillis(TimeUnit.MINUTES.toMillis(15))
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!hasGps(packageManager)) {
                    Log.e(TAG, "This hardware doesn't have GPS.")
                    // TODO: Handle the case when watch doesn't have GPS.
                    // Fall back to functionality that doesn't use location or
                    // warn the user that location function isn't available and
                    // to pair with their phone to receive location information.
                    val toast = Toast.makeText(
                        applicationContext,
                        "This hardware doesn't have GPS. Please pair with your phone to receive location information.",
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    return
                }
                // Get the last index in the locations array

                if (locationResult.locations.isEmpty()) {
                    Log.e(TAG, "LocationResult.locations is empty.")
                    return
                }

                sendLocationUpdate(locationResult.locations[locationResult.locations.lastIndex])
            }
        }

        if (Utilities.checkPermissions(this)) {

            Log.d(TAG, "Location permissions granted.")

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            Log.e(TAG, "Location permissions not granted.")
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        unbindFromAirportService()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}