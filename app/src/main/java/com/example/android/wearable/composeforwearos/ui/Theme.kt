package com.example.android.wearable.composeforwearos.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ExperimentalWearMaterial3Api
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
@OptIn(ExperimentalWearMaterial3Api::class)
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    MaterialTheme(
        colorScheme = colors,
        typography = WearAppTypography,
        content = content
    )
}



