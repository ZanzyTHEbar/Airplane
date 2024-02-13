package com.example.android.wearable.composeforwearos.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material3.AppCard
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.android.wearable.composeforwearos.R
import com.example.android.wearable.composeforwearos.ui.AppTheme

// TODO: Create a Button Composable (with a Row to center)
@Composable
fun ButtonExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        // Button
        Button(
            modifier = Modifier.size(ButtonDefaults.LargeIconSize),
            onClick = { /* ... */ },
        ) {
            Icon(
                imageVector = Icons.Rounded.Phone,
                contentDescription = "triggers phone action",
                modifier = iconModifier
            )
        }
    }
}

// TODO: Create a Text Composable
@Composable
fun TextExample(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        text = stringResource(R.string.device_shape)
    )
}

// TODO: Create a Card (specifically, an AppCard) Composable
@Composable
fun CardExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier,
        appImage = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Message,
                contentDescription = "triggers open message action",
                modifier = iconModifier
            )
        },
        appName = { Text("Messages") },
        time = { Text("12m") },
        title = { Text("Kim Green") },
        onClick = { /* ... */ }
    ) {
        Text("On my way!")
    }
}

// TODO: Create a Chip Composable
@Composable
fun ChipExample(
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier
) {
    Chip(
        modifier = modifier,
        onClick = { /* ... */ },
        label = {
            Text(
                text = "5 minute Meditation",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Rounded.SelfImprovement,
                contentDescription = "triggers meditation action",
                modifier = iconModifier
            )
        },
    )
}

// TODO: Create a ToggleChip Composable
@Composable
fun ToggleChipExample(modifier: Modifier = Modifier) {
    var checked by remember { mutableStateOf(true) }
    ToggleChip(
        modifier = modifier,
        checked = checked,
        toggleControl = {
            Switch(
                checked = checked,
                modifier = Modifier.semantics {
                    this.contentDescription = if (checked) "On" else "Off"
                },
                onCheckedChange = { checked = it }
            )
        },
        onCheckedChange = {
            checked = it
        },
        label = {
            Text(
                text = "Sound",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    )
}

// Function only used as a demo for when you start the code lab (removed as step 1).
@Composable
fun StartOnlyTextComposables() {
    Text(
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        text = stringResource(R.string.hello_world_starter)
    )
}

/* Previews of Composables. */

// Hello, world starter text preview
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = WearDevices.SMALL_ROUND
)
@Composable
fun StartOnlyTextComposablesPreview() {
    AppTheme {
        StartOnlyTextComposables()
    }
}

// Button Preview
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = WearDevices.SMALL_ROUND
)
@Composable
fun ButtonExamplePreview() {
    AppTheme {
        ButtonExample(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            iconModifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)
        )
    }
}

// Text Preview
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = WearDevices.SMALL_ROUND
)
@Composable
fun TextExamplePreview() {
    AppTheme {
        TextExample(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}

// Card Preview
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = WearDevices.SMALL_ROUND
)
@Composable
fun CardExamplePreview() {
    AppTheme {
        CardExample(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            iconModifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)
        )
    }
}


// Chip Preview
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = WearDevices.SMALL_ROUND,
)
@Composable
fun ChipExamplePreview() {
    AppTheme {
        ChipExample(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            iconModifier = Modifier
                .size(24.dp)
                .wrapContentSize(align = Alignment.Center)
        )
    }
}

// Toggle Chip Preview
@Preview(
    apiLevel = 26,
    uiMode = Configuration.UI_MODE_TYPE_WATCH,
    device = WearDevices.SMALL_ROUND
)
@Composable
fun ToggleChipExamplePreview() {
    AppTheme {
        ToggleChipExample(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
    }
}