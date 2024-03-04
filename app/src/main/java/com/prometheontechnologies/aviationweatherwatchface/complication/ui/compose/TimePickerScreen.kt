package com.prometheontechnologies.aviationweatherwatchface.complication.ui.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.horologist.composables.TimePicker
import com.prometheontechnologies.aviationweatherwatchface.complication.ui.MainViewModel
import java.time.LocalTime

@Composable
fun TimePickerScreen(
    viewModel: MainViewModel
) {
    TimePicker(
        modifier = Modifier
            .fillMaxSize(),
        onTimeConfirm = {
            viewModel.updateIntervalSetting(it)
        },
        time = LocalTime.MIDNIGHT,
        showSeconds = false
    )
}