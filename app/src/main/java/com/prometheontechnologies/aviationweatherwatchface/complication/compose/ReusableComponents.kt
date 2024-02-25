package com.prometheontechnologies.aviationweatherwatchface.complication.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleRight
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.AppCard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService

@Composable
fun ButtonWidget(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // Button
        Button(
            modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
            onClick = { onClick() },
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowCircleRight,
                contentDescription = "Triggers location permission request",
                modifier = iconModifier
            )
        }
    }
}

@Composable
fun ButtonLocationEnable(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // Button
        Button(
            modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
            onClick = { onClick() },
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "Triggers location service enabled action",
                modifier = iconModifier
            )
        }
    }
}

@Composable
fun ButtonStopLocation(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // Button
        Button(
            modifier = Modifier.size(ButtonDefaults.LargeButtonSize),
            onClick = { onClick() },
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOff,
                contentDescription = "Triggers location service disabled action",
                modifier = iconModifier
            )
        }
    }
}

@Composable
fun TextWidget(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = text
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CardWidget(
    context: Context,
    modifier: Modifier = Modifier,
    permissionState: PermissionState,
) {

    Intent(context, LocationUpdateService::class.java).apply {
        action =
            LocationUpdateService.Companion.ActionType.START.toString()
        context.startService(this)
    }

    AppCard(
        modifier = modifier,
        appName = { Text("Aviation Weather Watchface", color = Color.White) },
        title = { Text("Location Perms Set", color = Color.Yellow) },
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Manage the Location service from here",
                color = Color.White
            )
            Spacer(modifier = Modifier.size(5.dp))
            Row(horizontalArrangement = Arrangement.Center) {
                Image(
                    modifier = Modifier.height(24.dp),
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    colorFilter = ColorFilter.tint(Color.Blue),
                    contentDescription = "",
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = "It is recommended to grant 'All the time' for the best experience ",
                    color = Color.Yellow
                )
            }
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = "You can manually enable or disable location updates from here ",
                color = Color.Cyan
            )
            Spacer(modifier = Modifier.size(15.dp))
            /*ButtonWidget(
                onClick = { permissionState.launchPermissionRequest() }
            )*/

            Row(horizontalArrangement = Arrangement.Center) {
                ButtonLocationEnable(
                    onClick = {
                        if (LocationUpdateService.isRunning) {
                            Toast.makeText(
                                context,
                                "Location Updates already running",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ButtonLocationEnable
                        }
                        Intent(context, LocationUpdateService::class.java).apply {
                            action =
                                LocationUpdateService.Companion.ActionType.START.toString()
                            context.startService(this)
                        }

                        Toast.makeText(context, "Location Updates Started", Toast.LENGTH_SHORT)
                            .show()
                    }
                )
                Spacer(modifier = Modifier.size(8.dp))
                ButtonStopLocation(
                    onClick = {
                        if (!LocationUpdateService.isRunning) {
                            Toast.makeText(
                                context,
                                "Location Updates are not running",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ButtonStopLocation
                        }
                        Intent(context, LocationUpdateService::class.java).apply {
                            action =
                                LocationUpdateService.Companion.ActionType.STOP.toString()
                            context.startService(this)
                            Toast.makeText(context, "Location Updates Stopped", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                )
            }
        }
    }
}

