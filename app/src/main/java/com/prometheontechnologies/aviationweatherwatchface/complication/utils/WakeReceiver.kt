package com.prometheontechnologies.aviationweatherwatchface.complication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log

class WakeReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = WakeReceiver::class.java.simpleName
    }

    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var context: Context

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        this.context = context
        wakeUp(this.context)
    }

    private fun wakeUp(context: Context) {
        Log.d("WakeReceiver", "Wake up!")
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(
                PowerManager.ON_AFTER_RELEASE,
                "AviationWeatherWatchface:WakeReceiver"
            )
        wakeLock.acquire(5 * 1000L /* 5 seconds */)
    }
}