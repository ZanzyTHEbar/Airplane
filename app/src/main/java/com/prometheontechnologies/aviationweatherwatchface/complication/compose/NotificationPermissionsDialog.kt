package com.prometheontechnologies.aviationweatherwatchface.complication.compose

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.prometheontechnologies.aviationweatherwatchface.complication.theme.AviationWeatherWatchFaceTheme

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsPermissionsDialog(
    context: Context,
    finish: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    AviationWeatherWatchFaceTheme {

        val notificationsPermissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )

        val contentModifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
        val iconModifier: Modifier = Modifier
            .size(24.dp)
            .wrapContentSize(align = Alignment.Center)

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 32.dp,
                start = 8.dp,
                end = 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.Bottom,
            state = listState,
            //autoCentering = AutoCenteringParams(0, 0)
        ) {
            item { Spacer(modifier = Modifier.padding(20.dp)) }


            if (notificationsPermissionState.status.isGranted) {
                Toast.makeText(context, "Notifications permission granted", Toast.LENGTH_SHORT)
                    .show()

                val intent = Intent(context, LocationPermissionsDialogActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

                // close this activity
                finish()
            } else {
                val textToShow = if (notificationsPermissionState.status.shouldShowRationale) {
                    "In order to receive notifications, please grant the notification permission"
                } else {
                    "Notifications are required for the app to function properly"
                }

                item { TextWidget(contentModifier, textToShow) }
                item {
                    ButtonWidget(contentModifier, iconModifier, onClick = {
                        notificationsPermissionState.launchPermissionRequest()
                    })
                }
            }
        }
    }
}