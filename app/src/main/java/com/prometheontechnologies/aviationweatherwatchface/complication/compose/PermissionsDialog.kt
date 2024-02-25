package com.prometheontechnologies.aviationweatherwatchface.complication.compose

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
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
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.prometheontechnologies.aviationweatherwatchface.complication.theme.AviationWeatherWatchFaceTheme


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsDialog(
    context: Context,
    finish: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    AviationWeatherWatchFaceTheme {

        val locationPermissionsState = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        val backGroundPermissionState = rememberPermissionState(
            permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION
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

            if (locationPermissionsState.allPermissionsGranted) {
                item {
                    CardWidget(context, contentModifier, backGroundPermissionState)
                }
            } else {

                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val notifsEnabled = notificationManager.areNotificationsEnabled()

                if (!notifsEnabled) {
                    val intent = Intent(context, NotificationPermissionsDialogActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    finish()
                }

                val allPermissionsRevoked =
                    locationPermissionsState.permissions.size == locationPermissionsState.revokedPermissions.size

                val textToShow = if (!allPermissionsRevoked) {
                    "You must grant location permissions to continue"
                } else if (locationPermissionsState.shouldShowRationale) {
                    "Aviation Weather Watchface requires location permissions to function properly"
                } else {
                    "Getting your exact location is important for this app. " +
                            "Please grant us precise location, " +
                            "Thank you :D"
                }

                item { TextWidget(contentModifier, textToShow) }
                item {
                    ButtonWidget(contentModifier, iconModifier, onClick = {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    })
                }
            }
        }
    }
}
