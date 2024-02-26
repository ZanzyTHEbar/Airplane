package com.prometheontechnologies.aviationweatherwatchface.complication.compose

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.MaterialTheme
import com.prometheontechnologies.aviationweatherwatchface.complication.ManagerViewModel
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ServicesInterface
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.SettingItem
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.SettingType
import com.prometheontechnologies.aviationweatherwatchface.complication.services.LocationUpdateService
import com.prometheontechnologies.aviationweatherwatchface.complication.theme.AviationWeatherWatchFaceTheme

//TODO: Switch this to a nice UI using switches instead of buttons

@Composable
fun AppManager(
    context: Context,
    modifier: Modifier = Modifier,
    settingsList: List<SettingItem>,
    viewModel: ManagerViewModel
) {
    val complicationsSettings by viewModel.complicationsSettings.collectAsState()
    val listState = rememberScalingLazyListState()
    AviationWeatherWatchFaceTheme {
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
            item {
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
                        Row(horizontalArrangement = Arrangement.Center) {
                            Image(
                                modifier = Modifier.height(12.dp),
                                painter = painterResource(id = android.R.drawable.ic_dialog_info),
                                colorFilter = ColorFilter.tint(Color.Blue),
                                contentDescription = "Information Dialog Icon, black letter 'I' within a white circle",
                            )
                            Spacer(modifier = Modifier.size(5.dp))
                            Text(
                                text = "It is recommended to grant 'All the time' for the best experience ",
                                color = Color.Yellow
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.padding(20.dp)) }
            items(settingsList.size) { index ->
                SettingItemView(index, settingsList)
            }
        }
    }
}


@Composable
fun SettingItemView(
    index: Int,
    settingsList: List<SettingItem>
) {
    val setting = settingsList[index]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = setting.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        if (setting.enabled) {
            when (settingsList[index].type) {
                SettingType.SWITCH -> {
                    Switch(
                        checked =,
                        onCheckedChange = {

                        }
                    )
                }

                SettingType.BUTTON -> {}
                SettingType.DROPDOWN -> {}
            }
        }
    }
}


@Composable
fun AppManagerContent(
    context: Context,
    modifier: Modifier = Modifier
) {
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

            Row(horizontalArrangement = Arrangement.Center) {
                ButtonLocationEnable(
                    onClick = {

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
                                ServicesInterface.Companion.ActionType.STOP.toString()
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