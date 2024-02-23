package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationServices
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.data.AirportsDatabase
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
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
    }

    override fun onDestroy() {
        super.onDestroy()
        AirportsDatabase.destroyInstance()
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

        runBlocking {
            applicationContext.complicationsDataStore.updateData {
                it.copy(
                    complicationsDataStore = complicationData
                )
            }
        }
        Log.v(TAG, "Data updated: $complicationData")
    }

    private suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun start() {
        val notification = Utilities.notificationBuilder(
            this,
            getString(R.string.location_service_notification_channel_id),
            "Location Updates Active",
            "Location updates are active",
            android.R.drawable.ic_dialog_info
        )
            .setOngoing(true)
            .build()

        locationClient
            .getLocationUpdates(TimeUnit.SECONDS.toMillis(1))
            .catch { e -> e.printStackTrace() }
            .onEach { location ->

                Log.d(TAG, "Handling location update")

                val nearestAirportFlow = airportClient.getAirportUpdates(location)

                nearestAirportFlow.collect {

                    val (airport, weatherData) = it

                    val nearestAirport = airport.nearestAirport

                    val newComplicationData = ComplicationsDataStore(
                        ident = nearestAirport.ident,
                        distance = airport.distance,
                        temperature = weatherData.temp,
                        dewPoint = weatherData.dewPt,
                        windSpeed = weatherData.windSpeed,
                        windDirection = weatherData.windDirection
                    )
                    updateData(newComplicationData)
                }
            }
            .launchIn(serviceScope)

        startForeground(1, notification)
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
}