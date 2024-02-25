package com.prometheontechnologies.aviationweatherwatchface.complication.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.prometheontechnologies.aviationweatherwatchface.complication.compose.NotificationsPermissionsDialog

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