package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.activities.NotificationPermissionsDialogActivity
import com.prometheontechnologies.aviationweatherwatchface.complication.api.DefaultAirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.api.DefaultLocationClient
import com.prometheontechnologies.aviationweatherwatchface.complication.api.DefaultWeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.data.AirportsDatabase
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.AirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.LocationClient
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.LocationService
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.WeatherClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit

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

class LocationUpdateService : Service() {
    companion object {
        private val TAG = LocationUpdateService::class.java.simpleName
        const val UPDATE_LOCATION_DATA_ACTION =
            "com.prometheontechnologies.aviationweatherwatchface.complication.LOCATION_UPDATE"

        const val LOCATION_UPDATE =
            "com.prometheontechnologies.aviationweatherwatchface.complication.LOCATION_UPDATE"

        var isRunning = false

        enum class ActionType {
            START,
            STOP
        }
    }

    private lateinit var db: AirportsDatabase
    private lateinit var airportClient: AirportClient
    private lateinit var weatherClient: WeatherClient
    private lateinit var locationClient: LocationClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        airportClient =
            DefaultAirportClient(applicationContext, db.airportDAO(), weatherClient)

        db = AirportsDatabase.getDatabase(applicationContext)
        weatherClient = DefaultWeatherClient(applicationContext)
        isRunning = true
    }

    override fun onDestroy() {
        super.onDestroy()
        AirportsDatabase.destroyInstance()
        isRunning = false
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ActionType.START.toString() -> start()
            ActionType.STOP.toString() -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun broadcastLocationUpdate(location: Location) {
        val intent = Intent(UPDATE_LOCATION_DATA_ACTION)
        intent.putExtra(
            LOCATION_UPDATE,
            location
        )
        sendBroadcast(intent)
    }

    private suspend fun updateData(locationServiceData: LocationService, initialLoad: Boolean) {
        Log.d(TAG, "Updating data and notifying complications")
        Log.d(TAG, "Nearest Airport: ${locationServiceData.ident}")

        applicationContext.complicationsDataStore.updateData {
            it.copy(
                locationServiceDataStore = locationServiceData
            )
        }

        // Broadcast the location update
        broadcastLocationUpdate(locationServiceData.location)
        Utilities.requestComplicationUpdate(applicationContext, initialLoad)
    }

    @SuppressLint("WearRecents")
    private fun start() {
        locationClient
            .getLocationUpdates(TimeUnit.MINUTES.toMillis(1))
            .catch { e -> e.printStackTrace() }
            .onEach { locationData ->

                Log.d(TAG, locationData.location.toText())
                Log.d(TAG, "Handling location update")

                val nearestAirportFlow = airportClient.getAirportUpdates(locationData.location)

                nearestAirportFlow
                    .catch { e ->
                        e.printStackTrace()
                        Utilities.showToast("Error: ${e.message}", applicationContext)
                    }
                    .onEach { airport ->

                        val nearestAirport = airport.nearestAirport

                        val newComplicationData = LocationService(
                            ident = nearestAirport.ident,
                            location = locationData.location,
                            distance = airport.distance,
                        )

                        Log.d(TAG, "New Location Data: ${newComplicationData.toText()}")

                        updateData(newComplicationData, locationData.initialLoad)

                    }.launchIn(serviceScope)
            }
            .launchIn(serviceScope)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationsEnabled = notificationManager.areNotificationsEnabled()

        if (!notificationsEnabled) {
            // Request perms from the user
            val intent = Intent(this, NotificationPermissionsDialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            this.stop()
        }

        val notification = Utilities.notificationBuilder(
            this,
            getString(R.string.location_service_notification_channel_id),
            "Aviation Weather Watchface",
            "Location updates are active",
            android.R.drawable.ic_dialog_info
        )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .build()

        startForeground(100, notification)
        notificationManager.notify(100, notification)
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}