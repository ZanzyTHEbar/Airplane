package com.prometheontechnologies.aviationweatherwatchface.complication

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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
import com.prometheontechnologies.aviationweatherwatchface.complication.activities.NotificationPermissionsDialogActivity
import com.prometheontechnologies.aviationweatherwatchface.complication.complications.DistanceComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.complications.IDENTComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.complications.TempComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.complications.WindComplicationService
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ServicesInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Context.hasLocationPermissions(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

object Utilities {
    suspend fun showToast(message: String, context: Context) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun requestComplicationUpdate(context: Context, isInitialLoad: Boolean) {
        if (!isInitialLoad) {
            return
        }

        ComplicationDataSourceUpdateRequester
            .create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    DistanceComplicationService::class.java
                )
            )
            .requestUpdateAll()

        ComplicationDataSourceUpdateRequester
            .create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    IDENTComplicationService::class.java
                )
            )
            .requestUpdateAll()

        ComplicationDataSourceUpdateRequester
            .create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    TempComplicationService::class.java
                )
            )
            .requestUpdateAll()

        ComplicationDataSourceUpdateRequester
            .create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    WindComplicationService::class.java
                )
            )
            .requestUpdateAll()
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
    }

    fun notificationManagerBuilder(
        context: Context,
        service: ServicesInterface?
    ): NotificationManager {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationsEnabled = notificationManager.areNotificationsEnabled()

        if (!notificationsEnabled) {
            // Request perms from the user
            val intent = Intent(context, NotificationPermissionsDialogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            service?.stop() ?: Unit
        }

        return notificationManager
    }

    fun presentComplicationViews(
        context: Context,
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
                            context,
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
                            context,
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
}