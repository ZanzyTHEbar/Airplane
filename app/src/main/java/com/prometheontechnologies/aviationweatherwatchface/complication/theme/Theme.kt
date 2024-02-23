package com.prometheontechnologies.aviationweatherwatchface.complication.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Typography


val AviationWeatherWatchFaceTypography = Typography(
    defaultFontFamily = FontFamily.Default,
)

// TODO: Setup color pallet
val AviationWeatherWatchFaceColors: ColorScheme = ColorScheme(
    primary = Color.Unspecified,
    secondary = Color.Unspecified,
    background = Color.Unspecified,
    surface = Color.Unspecified,
    error = Color.Unspecified,
    onPrimary = Color.Unspecified,
    onSecondary = Color.Unspecified,
    onBackground = Color.Unspecified,
    onSurface = Color.Unspecified,
    onError = Color.Unspecified,

    )

@Composable
fun AviationWeatherWatchFaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        //colorScheme = AviationWeatherWatchFaceColors,
        typography = AviationWeatherWatchFaceTypography,
        content = content
    )
}