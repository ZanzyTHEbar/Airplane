package com.prometheontechnologies.aviationweatherwatchface.complication.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            runBlocking {
                // Start the location service
                //MainApp.locationUpdateService = Intent(context, LocationUpdateService::class.java).also {
                //    it.action = LocationUpdateService.ActionType.START.toString()
                //    context.startService(it)
                //}
            }
        }
    }

}