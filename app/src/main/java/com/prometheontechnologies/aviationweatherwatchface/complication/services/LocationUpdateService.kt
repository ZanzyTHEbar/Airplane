package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.activities.NotificationPermissionsDialogActivity
import com.prometheontechnologies.aviationweatherwatchface.complication.api.DefaultAirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.api.DefaultLocationClient
import com.prometheontechnologies.aviationweatherwatchface.complication.data.AirportsDatabase
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.AirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.LocationClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
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

        var isRunning = false

        enum class ActionType {
            START,
            STOP
        }
    }

    private lateinit var locationClient: LocationClient
    private lateinit var airportClient: AirportClient
    private lateinit var db: AirportsDatabase
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        db = AirportsDatabase.getDatabase(applicationContext)

        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

        airportClient = DefaultAirportClient(applicationContext, db.airportDAO())

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

    private suspend fun updateData(complicationData: ComplicationsDataStore) {
        Log.d(TAG, "Updating data and notifying complications")
        Log.d(TAG, "Nearest Airport: ${complicationData.ident}")

        applicationContext.complicationsDataStore.updateData {
            it.copy(
                complicationsDataStore = complicationData
            )
        }

        //LocationDataSingleton.updateLocationData(complicationData)
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("WearRecents")
    private fun start() {
        locationClient
            .getLocationUpdates(TimeUnit.MINUTES.toMillis(1))
            .catch { e -> e.printStackTrace() }
            .onEach { location ->

                Log.d(TAG, location.toText())
                Log.d(TAG, "Handling location update")

                val nearestAirportFlow = airportClient.getAirportUpdates(location)

                nearestAirportFlow
                    .catch { e ->
                        e.printStackTrace()
                        showToast("Error: ${e.message}")
                    }
                    .onEach { (airport, weatherData) ->

                        val nearestAirport = airport.nearestAirport

                        val newComplicationData = ComplicationsDataStore(
                            ident = nearestAirport.ident,
                            distance = airport.distance,
                            temperature = weatherData.temp,
                            dewPoint = weatherData.dewPt,
                            windSpeed = weatherData.windSpeed,
                            windDirection = weatherData.windDirection
                        )

                        Log.v(TAG, "New Complication Data: $newComplicationData")

                        updateData(newComplicationData)
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