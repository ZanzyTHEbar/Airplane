package com.prometheontechnologies.aviationweatherwatchface.complication.compose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi

class NotificationPermissionsDialogActivity : ComponentActivity() {
    companion object {
        private val TAG = NotificationPermissionsDialogActivity::class.java.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NotificationsPermissionsDialog(applicationContext, ::finish)
        }
    }
}