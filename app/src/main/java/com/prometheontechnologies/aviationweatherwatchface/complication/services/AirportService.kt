package com.prometheontechnologies.aviationweatherwatchface.complication.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.data.AirportsDatabase
import com.prometheontechnologies.aviationweatherwatchface.complication.data.ComplicationsDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.data.DataStoreModule
import com.prometheontechnologies.aviationweatherwatchface.complication.data.LocationUtil
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AirportService : Service() {

    companion object {
        private val TAG = AirportService::class.java.simpleName
    }

    private lateinit var messenger: Messenger
    private lateinit var db: AirportsDatabase
    private lateinit var locationUtil: LocationUtil
    private val appDataRepository: ComplicationsDataRepository by lazy {
        ComplicationsDataRepository.getInstance(
            DataStoreModule.provideProtoDataStore(applicationContext)
        )
    }
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        db = AirportsDatabase.getDatabase(this)
        locationUtil = LocationUtil(db.airportDAO())
        messenger = Messenger(IncomingHandler(Looper.getMainLooper(), this))
        Log.d(TAG, "AirportService bound successfully")
    }

    class IncomingHandler(looper: Looper, private val service: AirportService) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.d(TAG, "Message received: ${msg.what}")
            when (msg.what) {
                LOCATION_MESSAGE -> {
                    val data = msg.data
                    val location = Location("").apply {
                        latitude = data.getDouble("latitude")
                        longitude = data.getDouble("longitude")
                    }
                    service.handleLocationUpdate(location)
                }

                else -> super.handleMessage(msg)
            }
        }

        companion object {
            const val LOCATION_MESSAGE = 1
        }
    }

    private fun handleLocationUpdate(location: Location) {
        coroutineScope.launch {
            if (!Utilities.checkPermissions(applicationContext)) {
                showToast("Enable Location Permissions in settings")
                return@launch
            }

            locationUtil.currentLocation = location

            Log.d(
                TAG,
                "Location: ${locationUtil.currentLocation!!.latitude}, ${locationUtil.currentLocation!!.longitude}"
            )

            withContext(Dispatchers.IO) {
                // Simulate fetching nearest airport and weather data
                locationUtil.handleAirportCoroutine()
            }

            Log.d(TAG, "Handling location update")

            if (!locationUtil.nearestAirportLoaded || !locationUtil.weatherDataLoaded) {
                showToast("Failed to fetch nearest airport and weather data")
                return@launch
            }

            val weatherData = locationUtil.weatherData
            val nearestAirport = locationUtil.nearestAirportData.nearestAirport
            val distance = locationUtil.nearestAirportData.distance

            val newComplicationData = ComplicationsDataStore(
                ident = nearestAirport.ident,
                distance = distance,
                temperature = weatherData.temp,
                dewPoint = weatherData.dewPt,
                windSpeed = weatherData.windSpeed,
                windDirection = weatherData.windDirection
            )

            // Update your data store or UI here after fetching the data
            updateDataAndNotify(newComplicationData)
        }
    }

    private fun notifyDataUpdated() {
        val intent =
            Intent(resources.getString(R.string.complication_data_updated))
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // Call this method after you successfully save weather data and nearest airport data
    private fun updateDataAndNotify(complicationData: ComplicationsDataStore) {

        Log.d(TAG, "Updating data and notifying complications")
        Log.d(TAG, "Nearest Airport: ${complicationData.ident}")

        CoroutineScope(Dispatchers.IO).launch {
            // Update WeatherData and NearestAirport within your AppData in the DataStore
            appDataRepository.updateComplicationData(complicationData)
            // Notify that the data has been updated
            notifyDataUpdated()
        }
    }

    private fun showToast(message: String) {
        coroutineScope.launch(Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return messenger.binder
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel the scope to clean up any remaining jobs
    }
}