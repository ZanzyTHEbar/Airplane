package com.prometheontechnologies.aviationweatherwatchface.complication.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon.createWithResource
import android.provider.AlarmClock
import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.RangedValueComplicationData
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.data.TimeFormatComplicationText
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

class TimeComplicationService : SuspendingComplicationDataSourceService() {

    private fun openScreen(): PendingIntent? {
        val mClockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        mClockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        return PendingIntent.getActivity(
            this, 0, mClockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return Utilities.presentComplicationViews(
            this,
            type,
            getString(R.string.time_comp_desc),
            placeHolder,
            R.drawable.ic_clock
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val preferences: UserPreferences =
            applicationContext.complicationsDataStore.data.first().userPreferences

        val hour = LocalDateTime.now().hour
        val min = LocalDateTime.now().minute
        val progressVariable = hour * 60 + min.toFloat()

        val isMilitary = preferences.isMilitaryTime
        val leadingZero = preferences.isLeadingZeroTime

        val fmt = if (isMilitary && leadingZero) "HH:mm'z'"
        else if (!isMilitary && !leadingZero) "h:mm"
        else if (isMilitary) "H:mm"
        else "hh:mm"

        val text = TimeFormatComplicationText.Builder(format = fmt)
            .build()

        val textHandler =
            if (!isMilitary) TimeFormatComplicationText
                .Builder(format = "a")
                .build()
            else PlainComplicationText
                .Builder(text = "24h")
                .build()

        return when (request.complicationType) {

            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                text = text,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc))
                    .build()
            )
                .setTitle(textHandler)
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = createWithResource(
                            this,
                            R.drawable.ic_clock
                        )
                    ).build()
                )
                .setTapAction(openScreen())
                .build()

            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                text = text,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc))
                    .build()
            )
                .setTitle(textHandler)
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = createWithResource(
                            this,
                            R.drawable.ic_clock
                        )
                    ).build()
                )
                .setTapAction(openScreen())
                .build()

            ComplicationType.RANGED_VALUE -> RangedValueComplicationData.Builder(
                value = progressVariable,
                min = 0f,
                max = 1440f,
                contentDescription = PlainComplicationText.Builder(text = getString(R.string.time_comp_desc))
                    .build()
            )
                .setText(text)
                .setTitle(textHandler)
                .setMonochromaticImage(
                    MonochromaticImage.Builder(
                        image = createWithResource(
                            this,
                            R.drawable.ic_clock
                        )
                    ).build()
                )
                .setTapAction(openScreen())
                .build()

            else -> {
                if (Log.isLoggable(TAG, Log.WARN)) {
                    Log.w(TAG, "Unexpected complication type ${request.complicationType}")
                }
                null
            }
        }
    }

    companion object {
        private val TAG = TimeComplicationService::class.java.simpleName
        private const val placeHolder = "23:34"
    }
}

