package com.prometheontechnologies.aviationweatherwatchface.complication

import android.app.Application
import android.content.Intent
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService

/*
* App architecture:
*
* The MainApp class is the entry point for the application. It is responsible for creating the notification channel and starting the location update service.
* The location update service is a background service that retrieves the user's location and sends it to the AirportService to find the nearest airport.
* The AirportService is a foreground service that retrieves the nearest airport's weather data and sends it to the complications.
* The complications are services that can provide data to any watch face that asks for it.
* The complications are updated by the AirportService, which sends the data to the complications using binding.
* */
class MainApp : Application() {

    companion object {
        lateinit var locationUpdateService: Intent
        private val TAG = MainApp::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel
        //val channel = NotificationChannel(
        //    resources.getString(ResourcesR.string.location_service_notification_channel_id),
        //    "Aviation Weather Location Service",
        //    NotificationManager.IMPORTANCE_DEFAULT
        //)
        //val notificationManager =
        //    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //notificationManager.createNotificationChannel(channel)

        // Start the location service
        locationUpdateService = Intent(applicationContext, LocationUpdateService::class.java).also {
            it.action = LocationUpdateService.ActionType.START.toString()
            startService(it)
        }
        Log.d(TAG, "${locationUpdateService.action.toString()} started.")
    }
}