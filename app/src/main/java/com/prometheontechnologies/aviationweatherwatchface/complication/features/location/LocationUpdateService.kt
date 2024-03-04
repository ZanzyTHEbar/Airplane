package com.prometheontechnologies.aviationweatherwatchface.complication.features.location

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.LocationServices
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.AirportsDatabase
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.UserPreferencesRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.features.airport.AirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.features.airport.DefaultAirportClient
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto.LocationClient
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto.LocationData
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.dto.toText
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.DefaultWeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto.WeatherClient
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.NotificationHelper
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.requestComplicationUpdate
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationUpdateService : LifecycleService(), ServicesInterface {
    companion object {
        private val TAG = LocationUpdateService::class.java.simpleName
    }

    private lateinit var db: AirportsDatabase
    private lateinit var airportClient: AirportClient
    private lateinit var weatherClient: WeatherClient
    private lateinit var locationClient: LocationClient
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var repository: UserPreferencesRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override var serviceState: StateFlow<ServicesInterface.Companion.ServiceState>? = null

    override fun <T> setSettings(newSettings: T) {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        repository = UserPreferencesRepository(applicationContext)

        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext),
            repository
        )

        db = AirportsDatabase.getDatabase(applicationContext)
        weatherClient = DefaultWeatherClient(applicationContext)
        airportClient = DefaultAirportClient(applicationContext, db.airportDAO(), weatherClient)

        serviceState = MutableStateFlow(
            ServicesInterface.Companion.ServiceState(
                action = ServicesInterface.Companion.ActionType.START,
                isPaused = false,
                isRunning = true
            )
        )

        notificationHelper = NotificationHelper(applicationContext)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                serviceState?.onEach {
                    updateNotification()
                    if (it.action == ServicesInterface.Companion.ActionType.STOP || it.action == ServicesInterface.Companion.ActionType.NONE) return@onEach
                    updateNotification()
                    scheduleWakeAlarm()
                }?.launchIn(serviceScope)
            }
        }

        val notification = updateNotification()

        if (notification == null) {
            Log.e(TAG, "Notification is null")
            return
        }

        notificationHelper.notify(notification)
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        AirportsDatabase.destroyInstance()
        serviceState.let { stateFlow ->
            if (stateFlow == null) {
                return
            }
            stateFlow.value.isRunning = false
            stateFlow.value.isPaused = false
        }
        serviceScope.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ServicesInterface.Companion.ActionType.START.toString() -> start()
                ServicesInterface.Companion.ActionType.PAUSE.toString() -> pause()
                ServicesInterface.Companion.ActionType.RESUME.toString() -> resume()
                ServicesInterface.Companion.ActionType.STOP.toString() -> stop()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("WearRecents")
    override fun start() {

        serviceState.let { stateFlow ->
            if (stateFlow == null) {
                return
            }
            stateFlow.value.isPaused = false
            stateFlow.value.isRunning = true
            stateFlow.value.action = ServicesInterface.Companion.ActionType.ACTIVE
        }

        locationClient
            .getLocationUpdates()
            .catch { e -> e.printStackTrace() }
            .onEach { locationData ->

                Log.v(TAG, locationData.location.toText())
                Log.v(TAG, "Handling location update")

                val nearestAirportFlow = airportClient.getAirportUpdates(locationData.location)

                nearestAirportFlow
                    .catch { e ->
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            applicationContext.showToast("Error: ${e.message}", Toast.LENGTH_LONG)
                        }
                    }
                    .onEach { airport ->

                        val nearestAirport = airport.nearestAirport

                        val newComplicationData = LocationData(
                            ident = nearestAirport.ident,
                            location = locationData.location,
                            distance = airport.distance,
                        )

                        Log.d(TAG, "New Location Data: ${newComplicationData.toText()}")

                        updateData(newComplicationData, locationData.initialLoad)

                    }.launchIn(serviceScope)
            }
            .launchIn(serviceScope)

        val notification = updateNotification()

        if (notification == null) {
            Log.e(TAG, "Notification is null")
            return
        }

        notificationHelper.notify(notification)
        startForeground(NotificationHelper.NOTIFICATION_ID, notification)
    }

    override fun pause() {
        serviceState.let { stateFlow ->
            if (stateFlow == null) {
                return
            }
            stateFlow.value.isPaused = true
            stateFlow.value.action = ServicesInterface.Companion.ActionType.PAUSE
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun resume() {
        start()
    }

    override fun restart() {
        pause()
        resume()
    }

    override fun stop() {
        serviceState.let { stateFlow ->
            if (stateFlow == null) {
                return
            }
            stateFlow.value.isPaused = false
            stateFlow.value.isRunning = false
            stateFlow.value.action = ServicesInterface.Companion.ActionType.STOP
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun scheduleWakeAlarm() {
        Log.d(TAG, "Schedule wake alarm")
        /*val wakeIntent: PendingIntent =
            Intent(applicationContext, WakeReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    applicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }*/

        // TODO: Figure out how to schedule a wake alarm for this location service
        /*val remainingSeconds = currentTimeSeconds.value.toLong()
        val alarmTime = remainingSeconds - 5L
        val time = LocalDateTime.now().plusSeconds(alarmTime).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
        alarmManagerHelper.setAlarm(time, wakeIntent)*/
    }

    private fun updateNotification(): Notification? {
        serviceState.let { stateFlow ->
            if (stateFlow == null) return null
            if (stateFlow.value.action == ServicesInterface.Companion.ActionType.STOP || stateFlow.value.action == ServicesInterface.Companion.ActionType.NONE) return null

            return notificationHelper.generateNotification(
                stateFlow.value.action,
                isPaused = stateFlow.value.isPaused,
            )
        }
    }

    private fun updateData(locationServiceData: LocationData, initialLoad: Boolean) {
        Log.v(TAG, "Updating data and notifying complications")
        Log.v(TAG, "Nearest Airport: ${locationServiceData.ident}")

        LocalDataRepository.updateLocationData(locationServiceData)
        applicationContext.requestComplicationUpdate(initialLoad)
    }
}