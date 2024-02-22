package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import android.content.ComponentName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComplicationToggleArgs(
    val providerComponent: ComponentName,
    val complicationInstanceId: Int
) : Parcelable