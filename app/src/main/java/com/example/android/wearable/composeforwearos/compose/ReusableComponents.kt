/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.composeforwearos.compose


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.example.android.wearable.composeforwearos.R
import com.example.android.wearable.composeforwearos.dto.AppCardData
import com.example.android.wearable.composeforwearos.dto.NearestAirport

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
            modifier = Modifier.size(ButtonDefaults.LargeIconSize),
            onClick = { onClick() },
        ) {
            Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = "triggers phone action",
                modifier = iconModifier
            )
        }
    }
}

@Composable
fun WeatherInfoItem(icon: ImageVector, infoText: String, color: Color? = null) {
    Column(modifier = Modifier.padding(8.dp)) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = infoText,
            style = MaterialTheme.typography.bodyMedium,
            color = color ?: MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TextWidget(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        text = text
    )
}

@Composable
fun CardWidget(
    modifier: Modifier = Modifier,
    airport: NearestAirport,
    appCardData: AppCardData
) {
    AppCard(
        modifier = modifier
            .padding(8.dp),
        appName = { Text("Weather Details") },
        /*time = {
            Text(
                appCardData.time,
                color = if (appCardData.temp < 20) Color.Blue else Color.Red
            )
        },*/
        shape = MaterialTheme.shapes.medium,
        title = {
            Text(airport.nearestAirport.ident)
        },
        onClick = {}
    ) {

        val icon = if (appCardData.temp < 20.0) R.mipmap.cold else R.mipmap.hot

        // TODO: Create a Column of rows for the temperature and wind details
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherInfoItem(
                icon = Icons.Filled.Thermostat,
                infoText = "${appCardData.temp}°C",
                color = if (appCardData.temp < 20) Color.Blue else Color.Red
            )
            WeatherInfoItem(icon = Icons.Filled.Air, infoText = "${appCardData.windSpeed}km/h")
            WeatherInfoItem(
                icon = Icons.Filled.Explore,
                infoText = "${appCardData.windDirection}°"
            )
            /*Row {
                Text(
                    text = "Temperature: ${appCardData.temp}°C",
                    color = if (appCardData.temp < 20) Color.Blue else Color.Red
                )
            }
            Row {
                Text(
                    text = "Wind: ${appCardData.windSpeed} km/h",
                )
            }
            Row {
                Text(
                    text = "Wind: ${appCardData.windDirection}°"
                )
            }
            Row(horizontalArrangement = Arrangement.Center) {
                Image(
                    modifier = Modifier.height(20.dp),
                    painter = painterResource(id = icon),
                    contentDescription = "",
                )
                Spacer(modifier = Modifier.size(5.dp))
                Text(
                    text = "${appCardData.temp}°C"
                )
            }*/
        }

    }
}


