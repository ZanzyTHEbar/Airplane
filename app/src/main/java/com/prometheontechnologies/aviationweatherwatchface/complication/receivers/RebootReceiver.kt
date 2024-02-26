package com.prometheontechnologies.aviationweatherwatchface.complication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.hasLocationPermissions
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService

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

            val locationServiceIntentStop =
                Intent(context, LocationUpdateService::class.java).apply {
                    action =
                        ServicesInterface.Companion.ActionType.START.toString()
                }

            ContextCompat.startForegroundService(context, locationServiceIntentStop)

            ContextCompat.startForegroundService(context, locationServiceIntentStart)
            Log.d(TAG, "${Intent.ACTION_BOOT_COMPLETED} received")
        }
    }
}