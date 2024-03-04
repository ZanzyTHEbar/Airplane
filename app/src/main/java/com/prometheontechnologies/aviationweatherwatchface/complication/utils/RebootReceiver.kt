package com.prometheontechnologies.aviationweatherwatchface.complication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startForegroundService
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.features.location.LocationUpdateService

class RebootReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = RebootReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action || Intent.ACTION_MY_PACKAGE_UNSUSPENDED == intent.action) {

            if (!context.hasLocationPermissions()) {
                Log.e(TAG, "Location permissions not granted")
                return
            }

            val locationServiceIntentStart =
                Intent(context, LocationUpdateService::class.java).apply {
                    action =
                        ServicesInterface.Companion.ActionType.START.toString()
                }

            startForegroundService(context, locationServiceIntentStart)
            Log.d(TAG, "${Intent.ACTION_BOOT_COMPLETED} received")
        }
    }
}