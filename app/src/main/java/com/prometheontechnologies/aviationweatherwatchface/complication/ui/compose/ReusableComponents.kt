package com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowCircleRight
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

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


