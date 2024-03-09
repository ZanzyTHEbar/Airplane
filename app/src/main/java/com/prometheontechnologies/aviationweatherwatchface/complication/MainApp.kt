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
 * TODO: Fix cloudlayer complications styling
 * TODO: Add onclick for ident that opens a screen with the full METAR report.
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