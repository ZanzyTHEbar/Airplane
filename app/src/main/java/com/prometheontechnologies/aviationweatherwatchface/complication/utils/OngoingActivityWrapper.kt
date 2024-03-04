package com.prometheontechnologies.aviationweatherwatchface.complication.utils

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.ServicesInterface

interface OngoingActivityWrapper {
    fun allowForegroundService(): Boolean

    fun setOngoingActivity(
        locationUpdateServiceState: ServicesInterface.Companion.ActionType,
        applicationContext: Context,
        onTouchIntent: PendingIntent,
        message: String,
        notificationBuilder: NotificationCompat.Builder
    )
}