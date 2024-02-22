package com.prometheontechnologies.aviationweatherwatchface.complication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData

class Utilities {
    companion object {

        private val TAG = Utilities::class.java.simpleName

        fun checkPermissions(context: Context): Boolean {
            val locationPerms = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "Location permissions: $locationPerms")

            return locationPerms
            /*return ActivityCompat.checkSelfPermission(
                contextContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                contextContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                contextContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED*/
        }

        fun notificationBuilder(
            context: Context,
            channelId: String,
            title: String,
            message: String,
            icon: Int
        ): NotificationCompat.Builder {
            return NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(icon)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        // TODO: Implement custom Icons
        fun presentPreviewData(
            context: Context,
            type: ComplicationType,
            description: String,
            placeholderFirst: String,
            placeholderSecond: String,
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
                                context,
                                R.drawable.ic_clock
                            )
                        ).build()
                    )
                    .setTapAction(null)
                    .build()

                ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                    text = PlainComplicationText.Builder(text = placeholderFirst).build(),
                    contentDescription = PlainComplicationText.Builder(text = description)
                        .build()
                )
                    .setMonochromaticImage(
                        MonochromaticImage.Builder(
                            image = Icon.createWithResource(
                                context,
                                R.drawable.ic_clock
                            )
                        ).build()
                    )
                    .setTapAction(null)
                    .build()

                ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                    value = 608f,
                    min = 0f,
                    max = 1440f,
                    contentDescription = PlainComplicationText.Builder(text = description)
                        .build()
                )
                    .setText(PlainComplicationText.Builder(text = placeholderFirst).build())
                    .setTitle(PlainComplicationText.Builder(text = placeholderSecond).build())
                    .setMonochromaticImage(
                        MonochromaticImage.Builder(
                            image = Icon.createWithResource(
                                context,
                                R.drawable.ic_clock
                            )
                        ).build()
                    )
                    .setTapAction(null)
                    .build()

                else -> {
                    null
                }
            }
        }
    }
}