package com.prometheontechnologies.aviationweatherwatchface.complication.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.data.dto.ServicesInterface

class NotificationHelper(
    private val applicationContext: Context
) {
    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Location Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        //channel.description = "Used to display ongoing location sharing notifications."
        notificationManager.createNotificationChannel(channel)
    }

    /*private fun activityLauncherIntent(): PendingIntent {
        val launchActivityIntent = Intent(applicationContext, mainActivity)
        launchActivityIntent.putExtra(EXTRA_LAUNCH_FROM_NOTIFICATION, true)
        launchActivityIntent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
            applicationContext,
            1,
            launchActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }*/

    private fun pauseIntent(): PendingIntent {
        val pauseIntent = Intent()
        pauseIntent.action = ServicesInterface.Companion.ActionType.PAUSE.toString()
        return PendingIntent.getBroadcast(
            applicationContext,
            1,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun resumeIntent(): PendingIntent {
        val resumeIntent = Intent()
        resumeIntent.action = ServicesInterface.Companion.ActionType.RESUME.toString()
        return PendingIntent.getBroadcast(
            applicationContext,
            1,
            resumeIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopIntent(): PendingIntent {
        val stopIntent = Intent()
        stopIntent.action = ServicesInterface.Companion.ActionType.STOP.toString()
        return PendingIntent.getBroadcast(
            applicationContext,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun restartIntent(): PendingIntent {
        val stopIntent = Intent()
        stopIntent.action = ServicesInterface.Companion.ActionType.RESUME.toString()
        return PendingIntent.getBroadcast(
            applicationContext,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setNotificationActions(
        locationServiceState: ServicesInterface.Companion.ActionType,
        isPaused: Boolean,
        notificationBuilder: NotificationCompat.Builder
    ) {
        Log.v(
            "NotificationHelper",
            "setNotificationActions $locationServiceState $isPaused"
        )
        val style = MediaStyle().setMediaSession(null)
        when {
            isPaused -> {
                style.setShowActionsInCompactView(0, 1)
                notificationBuilder
                    .addAction(
                        android.R.drawable.ic_media_play,
                        "resume",
                        resumeIntent()
                    )
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "stop",
                        stopIntent()
                    )
            }

            locationServiceState == ServicesInterface.Companion.ActionType.ACTIVE -> {
                Log.v("NotificationHelper", "setNotificationActions ACTIVE")
                style.setShowActionsInCompactView(0, 1)
                notificationBuilder
                    .addAction(
                        android.R.drawable.ic_media_pause,
                        "pause",
                        restartIntent()
                    )
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "stop",
                        stopIntent()
                    )
            }

            else -> {
                style.setShowActionsInCompactView(0, 1)
                notificationBuilder
                    .addAction(
                        android.R.drawable.ic_media_pause,
                        "pause",
                        pauseIntent()
                    )
                    .addAction(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "stop",
                        stopIntent()
                    )
            }
        }
        notificationBuilder.setStyle(style)
    }

    fun generateNotification(
        locationServiceState: ServicesInterface.Companion.ActionType,
        isPaused: Boolean
    ): Notification {
        val titleText = getTitleText(locationServiceState)
        val contentText = getContentText(locationServiceState)

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(titleText)
                .setContentText(contentText)
                //.setContentIntent(activityLauncherIntent())
                .setSmallIcon(R.drawable.ic_distance)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_LOCATION_SHARING)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        setNotificationActions(locationServiceState, isPaused, notificationBuilder)

        when (locationServiceState) {
            ServicesInterface.Companion.ActionType.START -> applicationContext.getColor(R.color.start_color)
            ServicesInterface.Companion.ActionType.ACTIVE -> applicationContext.getColor(R.color.active_color)
            ServicesInterface.Companion.ActionType.RESUME -> applicationContext.getColor(R.color.resume_color)
            ServicesInterface.Companion.ActionType.PAUSE -> applicationContext.getColor(R.color.pause_color)
            ServicesInterface.Companion.ActionType.STOP -> applicationContext.getColor(R.color.stop_color)
            else -> applicationContext.getColor(R.color.start_color)
        }.let {
            notificationBuilder.setColor(it)
        }

        // TODO: Implement ongoingActivityWrapper https://github.com/lucianosantosdev/IntervalTimer
        /*ongoingActivityWrapper.setOngoingActivity(
            locationServiceState = locationServiceState,
            applicationContext = applicationContext,
            onTouchIntent = activityLauncherIntent(),
            message = "$titleText · $contentText",
            notificationBuilder = notificationBuilder
        )*/
        return notificationBuilder.build()
    }

    private fun getContentText(locationServiceState: ServicesInterface.Companion.ActionType): String {
        return when (locationServiceState) {
            ServicesInterface.Companion.ActionType.START -> {
                applicationContext.getString(R.string.state_start_text)
            }

            ServicesInterface.Companion.ActionType.ACTIVE -> {
                applicationContext.getString(R.string.state_active_text)
            }

            ServicesInterface.Companion.ActionType.PAUSE -> {
                applicationContext.getString(R.string.state_pause_text)
            }

            ServicesInterface.Companion.ActionType.RESUME -> {
                applicationContext.getString(R.string.state_resume_text)
            }

            ServicesInterface.Companion.ActionType.STOP -> {
                applicationContext.getString(R.string.state_stop_text)
            }

            ServicesInterface.Companion.ActionType.NONE -> {
                ""
            }
        }
    }

    private fun getTitleText(locationServiceState: ServicesInterface.Companion.ActionType): String {
        return when (locationServiceState) {
            ServicesInterface.Companion.ActionType.START -> applicationContext.getString(R.string.state_start_text)
            ServicesInterface.Companion.ActionType.ACTIVE -> applicationContext.getString(R.string.state_active_text)
            ServicesInterface.Companion.ActionType.PAUSE -> applicationContext.getString(R.string.state_pause_text)
            ServicesInterface.Companion.ActionType.RESUME -> applicationContext.getString(R.string.state_resume_text)
            ServicesInterface.Companion.ActionType.STOP -> applicationContext.getString(R.string.state_stop_text)
            ServicesInterface.Companion.ActionType.NONE -> ""
        }
    }

    fun notify(notification: Notification) {
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    companion object {
        const val NOTIFICATION_ID = 100
        const val EXTRA_LAUNCH_FROM_NOTIFICATION = "EXTRA_LAUNCH_FROM_NOTIFICATION"
        private const val NOTIFICATION_CHANNEL_ID = "aviation_weather_location_service"
    }
}