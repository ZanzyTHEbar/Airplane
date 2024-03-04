package com.prometheontechnologies.aviationweatherwatchface.complication.utils

import android.Manifest
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.prometheontechnologies.aviationweatherwatchface.complication.features.complications.DistanceComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.features.complications.IDENTComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.features.complications.TempComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.features.complications.WindComplicationService


fun Context.hasLocationPermissions(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.presentComplicationViews(
    type: ComplicationType,
    description: String,
    placeholderFirst: String,
    resID: Int,
    tapAction: PendingIntent? = null
): ComplicationData? {
    return when (type) {
        ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = placeholderFirst).build(),
            contentDescription = PlainComplicationText.Builder(text = description)
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = Icon.createWithResource(
                        this,
                        resID
                    )
                ).build()
            )
            .setTapAction(tapAction)
            .build()

        ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text = placeholderFirst).build(),
            contentDescription = PlainComplicationText.Builder(text = description)
                .build()
        )
            .setMonochromaticImage(
                MonochromaticImage.Builder(
                    image = Icon.createWithResource(
                        this,
                        resID
                    )
                ).build()
            )
            .setTapAction(tapAction)
            .build()

        else -> {
            null
        }
    }
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

fun Context.requestComplicationUpdate(isInitialLoad: Boolean) {
    if (!isInitialLoad) {
        return
    }

    ComplicationDataSourceUpdateRequester
        .create(
            context = this,
            complicationDataSourceComponent = ComponentName(
                this,
                DistanceComplicationService::class.java
            )
        )
        .requestUpdateAll()

    ComplicationDataSourceUpdateRequester
        .create(
            context = this,
            complicationDataSourceComponent = ComponentName(
                this,
                IDENTComplicationService::class.java
            )
        )
        .requestUpdateAll()

    ComplicationDataSourceUpdateRequester
        .create(
            context = this,
            complicationDataSourceComponent = ComponentName(
                this,
                TempComplicationService::class.java
            )
        )
        .requestUpdateAll()

    ComplicationDataSourceUpdateRequester
        .create(
            context = this,
            complicationDataSourceComponent = ComponentName(
                this,
                WindComplicationService::class.java
            )
        )
        .requestUpdateAll()
}

fun Context.notificationBuilder(
    channelId: String,
    title: String,
    message: String,
    icon: Int
): NotificationCompat.Builder {
    return NotificationCompat.Builder(this, channelId)
        .setContentTitle(title)
        .setContentText(message)
        .setSmallIcon(icon)
}