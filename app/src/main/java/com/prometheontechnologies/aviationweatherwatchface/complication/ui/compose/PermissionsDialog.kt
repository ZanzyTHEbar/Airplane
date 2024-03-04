package com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.Destinations

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun <T> PermissionsScreen(
    permissionState: T,
    navController: NavHostController
) {
    val contentModifier = Modifier
        .fillMaxSize()
        .padding(bottom = 8.dp)
    val iconModifier: Modifier = Modifier
        .size(24.dp)
        .wrapContentSize(align = Alignment.Center)

    var textToShow = ""
    var executeAction: () -> Unit = {}

    when (permissionState) {
        is PermissionState -> {
            if (permissionState.status.isGranted) {
                // TODO: navigate to the settings screen
                navController.navigate(Destinations.SettingsScreen)
            } else {
                textToShow = if (permissionState.status.shouldShowRationale) {
                    "In order to receive notifications, please grant the notification permission"
                } else {
                    "Notifications are required for the app to function properly"
                }
            }

            executeAction = {
                permissionState.launchPermissionRequest()
            }
        }

        is MultiplePermissionsState -> {
            if (permissionState.allPermissionsGranted) {
                navController.navigate(Destinations.NotifactionPerms)
            } else {

                val allPermissionsRevoked =
                    permissionState.permissions.size == permissionState.revokedPermissions.size

                textToShow = if (!allPermissionsRevoked) {
                    "You must grant location permissions to continue"
                } else if (permissionState.shouldShowRationale) {
                    "Aviation Weather Watchface requires location permissions to function properly"
                } else {
                    "Getting your exact location is important for this app. " +
                            "Please grant us precise location, " +
                            "Thank you :D"
                }
            }

            executeAction = {
                permissionState.launchMultiplePermissionRequest()
            }
        }

        else -> {
            TextWidget(contentModifier, "Unknown permission state")
        }
    }

    Column(
        modifier = contentModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.padding(12.dp))
        TextWidget(contentModifier, textToShow)
        Spacer(modifier = Modifier.padding(8.dp))
        ButtonWidget(
            contentModifier,
            iconModifier,
            onClick = {
                executeAction()
            })
    }
}