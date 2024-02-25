package com.prometheontechnologies.aviationweatherwatchface.complication.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.prometheontechnologies.aviationweatherwatchface.complication.compose.PermissionsDialog


class LocationPermissionsDialogActivity : ComponentActivity() {

    companion object {
        private val TAG = LocationPermissionsDialogActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PermissionsDialog(applicationContext, ::finish)
        }
    }
}
