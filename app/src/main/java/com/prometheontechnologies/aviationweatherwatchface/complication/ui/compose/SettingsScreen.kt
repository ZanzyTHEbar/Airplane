package com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.compose.layout.ScalingLazyColumn
import com.google.android.horologist.compose.layout.ScalingLazyColumnDefaults
import com.google.android.horologist.compose.layout.rememberColumnState
import com.google.android.horologist.compose.material.Chip
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingItem
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.SettingType
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.Destinations
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.MainViewModel

//TODO: Switch this to a nice UI using switches instead of buttons

@Composable
fun SettingsScreen(
    context: Context,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    navController: NavHostController
) {
    //val complicationsSettings by viewModel.complicationsSettings.collectAsState()

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is MainViewModel.SettingsUIState.Loading -> CircularProgressIndicator()
        is MainViewModel.SettingsUIState.Error -> {
            Toast.makeText(context, "Error Loading Settings", Toast.LENGTH_SHORT).show()
        }

        is MainViewModel.SettingsUIState.SettingsLoaded -> {
            SettingsList(
                settings = (uiState as MainViewModel.SettingsUIState.SettingsLoaded).settings,
                viewModel = viewModel,
                navController = navController
            )
        }

        else -> {}
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SettingsList(
    settings: List<SettingItem>,
    viewModel: MainViewModel,
    navController: NavHostController
) {

    val columnState =
        rememberColumnState(
            ScalingLazyColumnDefaults.responsive(
                firstItemIsFullWidth = false
            )
        )
    ScalingLazyColumn(columnState = columnState) {
        item {
            ListHeader {
                Text(
                    text = "Aviation Weather Manager",
                    modifier = Modifier.fillMaxWidth(0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
        item { Spacer(modifier = Modifier.padding(8.dp)) }
        items(settings.size) { index ->
            SettingItemView(index, settings, viewModel)
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Chip(
                    label = "Set Update Time",
                    onClick = {
                        navController.navigate(Destinations.TimePicker)
                    },
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth(),
                    enabled = true,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.ArrowCircleRight,
                            contentDescription = "Triggers location permission request",
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center)
                        )
                    }
                )
            }
        }
        item { InfoCard() }
    }
}

@OptIn(ExperimentalHorologistApi::class)
@Composable
fun SettingItemView(
    index: Int,
    settingsList: List<SettingItem>,
    viewModel: MainViewModel
) {
    val setting = settingsList[index]
    if (!setting.enabled) return

    val locationServiceButton by viewModel.locationServicesButtonEnabled.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = setting.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        when (setting.type) {
            SettingType.SWITCH -> {
                Switch(
                    checked = if (setting.id == 0) locationServiceButton else setting.checked,
                    onCheckedChange = { checked ->
                        viewModel.updateSwitchesSetting(
                            settingId = setting.id,
                            checked = checked
                        )
                    }
                )
            }

            else -> {}
        }
    }
}

@Composable
fun InfoCard(
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.padding(
            top = 12.dp
        ),
        appName = { Text("Aviation Weather Watchface", color = Color.White) },
        title = { Text("Location Perms Set", color = Color.Yellow) },
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = 12.dp,
                    start = 6.dp,
                    end = 6.dp,
                    bottom = 12.dp
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
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
