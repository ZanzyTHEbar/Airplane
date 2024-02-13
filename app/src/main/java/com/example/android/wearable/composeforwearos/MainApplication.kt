package com.example.android.wearable.composeforwearos

import android.app.Application
import androidx.work.Configuration
import com.example.android.wearable.composeforwearos.data.AirportDAO
import com.example.android.wearable.composeforwearos.data.AirportsDatabase

class MainApplication : Application(), Configuration.Provider {

    companion object {
        lateinit var db: AirportsDatabase
        lateinit var onDestroy: () -> Unit
        lateinit var dao: AirportDAO
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()

    override fun onCreate() {
        super.onCreate()
        db = AirportsDatabase.getDatabase(this)
        onDestroy = { AirportsDatabase.destroyInstance() }
        dao = db.airportDAO()
    }
}