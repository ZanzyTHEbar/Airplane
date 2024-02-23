package com.prometheontechnologies.aviationweatherwatchface.complication.complications

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import kotlinx.coroutines.flow.first

class WindComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        Log.d(TAG, "Complication Activated: $complicationInstanceId")
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return Utilities.presentPreviewData(
            this,
            type,
            description,
            placeHolder,
            UNIT
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val complicationsDataStore = applicationContext
            .complicationsDataStore
            .data
            .first()
            .complicationsDataStore

        val text = "${complicationsDataStore.windSpeed}/${complicationsDataStore.windDirection}"

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                ShortTextComplicationData
                    .Builder(
                        text = PlainComplicationText.Builder(text).build(),
                        contentDescription = PlainComplicationText.Builder(description).build()
                    )
                    .build()
            }

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    PlainComplicationText.Builder(
                        "${description}: ${complicationsDataStore.temperature}${UNIT}"
                    ).build(),
                    PlainComplicationText.Builder(description).build()
                )
                    .build()
            }

            else -> {
                Log.e(TAG, "Unexpected complication type: ${request.complicationType}")
                null
            }
        }
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Complication Deactivated: $complicationInstanceId")
    }

    companion object {
        private val TAG = TempComplicationService::class.java.simpleName
        private const val UNIT = "nts/°"
        private const val description = "Wind"
        private const val placeHolder = "23/15"
    }
}