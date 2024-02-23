package com.prometheontechnologies.aviationweatherwatchface.complication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService
import com.prometheontechnologies.aviationweatherwatchface.complication.services.hasLocationPermissions

class RebootReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = RebootReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {

            if (!context.hasLocationPermissions()) {
                Log.e(TAG, "Location permissions not granted")
                return
            }

            Intent(context, LocationUpdateService::class.java).apply {
                action =
                    LocationUpdateService.Companion.ActionType.START.toString()
                context.startService(this)
            }
            Log.d(TAG, "${Intent.ACTION_BOOT_COMPLETED} received")
        }
    }
}