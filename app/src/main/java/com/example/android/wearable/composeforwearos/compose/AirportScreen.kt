package com.example.android.wearable.composeforwearos.compose

import android.Manifest
import android.util.Log
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
//import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.scrollAway
//import androidx.wear.compose.material3.Icon
//import androidx.wear.compose.material3.IconButton
//import androidx.wear.compose.material3.MaterialTheme
//import androidx.wear.compose.material3.RadioButton
//import androidx.wear.compose.material3.RadioButtonDefaults
//import androidx.wear.compose.material3.Text
//import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import com.example.android.wearable.composeforwearos.MainApplication
//import com.example.android.wearable.composeforwearos.data.AirportsEvent
//import com.example.android.wearable.composeforwearos.data.AirportsState
//import com.example.android.wearable.composeforwearos.data.SortType
import com.example.android.wearable.composeforwearos.ui.AppTheme
//import com.example.android.wearable.composeforwearos.ui.WearAppTheme
import com.example.android.wearable.composeforwearos.utilities.LocationUtil
import com.example.android.wearable.composeforwearos.viewmodels.MainViewModel
import com.example.android.wearable.composeforwearos.viewmodels.viewModelFactory
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AirportScreen(
    locationUtil: LocationUtil,
) {
    AppTheme(
        useDarkTheme = true
    ) {

        val viewModel = viewModel<MainViewModel>(
            factory = viewModelFactory {
                MainViewModel(MainApplication.dao, locationUtil)
            }
        )


        val listState = rememberScalingLazyListState()

        val locationPermissionsState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        Scaffold(
            timeText = {
                TimeText(modifier = Modifier.scrollAway(listState))
            },
            vignette = {
                // Only show a Vignette for scrollable screens. This code lab only has one screen,
                // which is scrollable, so we show it all the time.
                Vignette(vignettePosition = VignettePosition.TopAndBottom)
            },
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            }
        ) {
            // Modifiers used by our Wear composables.
            val contentModifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
            val iconModifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)

            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                autoCentering = AutoCenteringParams(),
                verticalArrangement = Arrangement.Bottom,
                contentPadding = PaddingValues(
                    top = 32.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 32.dp
                ),
                state = listState
            ) {
                item { Spacer(modifier = Modifier.size(20.dp)) }
                if (locationPermissionsState.allPermissionsGranted) {
                    Log.d(
                        "Location",
                        "Location: ${locationUtil.nearestAirportData.value.nearestAirport.name} - IATA: ${locationUtil.nearestAirportData.value.nearestAirport.iataCode} - IDENT: ${locationUtil.nearestAirportData.value.nearestAirport.ident} - Distance: ${locationUtil.nearestAirportData.value.distance}"
                    )
                    if (!locationUtil.locationDataLoaded.value)
                        item {
                            // TODO: Add a loading indicator
                            TextWidget(
                                contentModifier,
                                "Thanks! I can access your exact location :D"
                            )
                        }
                    else {
                        item {
                            CardWidget(
                                modifier = contentModifier,
                                appCardData = locationUtil.locationData.value,
                                airport = locationUtil.nearestAirportData.value
                            )
                        }
                    }
                } else {
                    val allPermissionsRevoked =
                        locationPermissionsState.permissions.size ==
                                locationPermissionsState.revokedPermissions.size

                    val textToShow = if (!allPermissionsRevoked) {
                        "Yay! Thanks for letting me access your approximate location. " +
                                "But you know what would be great? If you allow me to know where you " +
                                "exactly are. Thank you!"
                    } else if (locationPermissionsState.shouldShowRationale) {
                        "Getting your exact location is important for this apps accuracy. " +
                                "Please grant us fine location. Thank you :D"
                    } else {
                        "This feature requires location permissions"
                    }
                    item { TextWidget(contentModifier, textToShow) }
                    item {
                        ButtonWidget(contentModifier, iconModifier) {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    }
                }
            }
        }
    }
}

/*item {
                    Row(
             modifier = Modifier
                 .fillMaxWidth()
                 .horizontalScroll(rememberScrollState()),
             verticalAlignment = Alignment.CenterVertically
         ) {
             SortType.entries.forEach { sortType ->
                 Row(
                     modifier = Modifier
                         .padding(8.dp)
                         .clickable {
                             onEvent(AirportsEvent.SortAirports(sortType))
                         },
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     RadioButton(
                         selected = state.sortType == sortType,
                         onSelected = {
                             onEvent(AirportsEvent.SortAirports(sortType))
                         }, colors = RadioButtonDefaults.radioButtonColors(
                             selectedContainerColor = MaterialTheme.colorScheme.primary,
                             unselectedContainerColor = MaterialTheme.colorScheme.onSurface
                         ),
                         label = {
                             Text(
                                 text = sortType.name,
                                 maxLines = 1,
                                 overflow = TextOverflow.Ellipsis
                             )
                         }
                     )
                 }
             }
         }
     }    items(state.airports) { airport ->        Row(            modifier = contentModifier.height(176.dp),            verticalAlignment = Alignment.CenterVertically        ) {            Column(                modifier = Modifier.weight(1f),                verticalArrangement = Arrangement.spacedBy(8.dp),            ) {                Text(                    text = "${airport.name} - ${airport.iataCode}",                    maxLines = 1,                    overflow = TextOverflow.Ellipsis,                    fontSize = 20.sp,                    color = MaterialTheme.colorScheme.primary                )                *//*IconButton(onClick = {                onEvent(AirportsEvent.Refresh)            }) {                Icon(
                     imageVector = Icons.Default.Refresh,
                     contentDescription = "Refresh",
                     modifier = iconModifier
                 )
             }*//*
         }
     }
 }*/
//item { TextExample(contentModifier) }
//item { ButtonExample(contentModifier, iconModifier) }
//item { CardExample(contentModifier, iconModifier) }
//item { ChipExample(contentModifier, iconModifier) }
//item { ToggleChipExample(contentModifier) }