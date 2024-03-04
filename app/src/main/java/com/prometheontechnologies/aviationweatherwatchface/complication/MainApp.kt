package com.prometheontechnologies.aviationweatherwatchface.complication

import android.app.Application
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.NotificationHelper

/*
* App architecture:
*
* The MainApp class is the entry point for the application. It is responsible for creating the notification channel.
* The location update service is a background service that retrieves the user's location and stores it in a shared repository.
* The AirportClient is an object that contains business logic to retrieve the nearest airport and store the ident, location, and distance in the shared repo.
* The complications are services that can provide data to any watch face that asks for it.
* The complications are updated via a timer, and a manual update when new data is available.
* WeatherUpdateWorker is a worker that updates the weather data in the shared repo.
* */

/**
 * Complications:
 * TODO: Density Altitude complication (individual feet)
 * TODO: Cloud Layer complication
 * TODO: Visibility complication (statute miles)
 * TODO: Pressure complication (inches of mercury)
 * Preferences:
 * TODO: Flying mode - toggle between flying and driving mode, set an interval for the weather updates and the location updates (in minutes) based on the mode.
 * TODO: Force and update after sleep mode (if the watch is in sleep mode for a long time, force an update when it wakes up).
 */

class MainApp : Application() {
    companion object {
        private val TAG = MainApp::class.java.simpleName
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper(applicationContext).createNotificationChannel()
    }
}