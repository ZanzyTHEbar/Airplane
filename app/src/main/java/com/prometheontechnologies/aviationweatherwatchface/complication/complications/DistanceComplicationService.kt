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
import kotlin.math.roundToInt

class DistanceComplicationService : SuspendingComplicationDataSourceService() {

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        Log.d(TAG, "Complication Activated: $complicationInstanceId")
    }

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return Utilities.presentPreviewData(
            this,
            type,
            description,
            "23",
            UNIT
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        /*// we pass the complication id, so we can only handle the request for the complication id
        val thisDataSource = ComponentName(this, javaClass)
        val complicationPendingIntent = ReceiverService.getToggleIntent(
            this,
            thisDataSource,
            request.complicationInstanceId
        )*/

        val complicationsDataStore = applicationContext
            .complicationsDataStore
            .data
            .first()
            .complicationsDataStore

        Log.v(TAG, "complicationsDataStore: $complicationsDataStore")

        val distance = complicationsDataStore.distance / NAUTICAL_MILES_CONSTANT
        val text = "${distance.roundToInt()}${UNIT}"

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
                        "${description}: $text"
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
        private val TAG = DistanceComplicationService::class.java.simpleName
        private const val NAUTICAL_MILES_CONSTANT = 1.852
        private const val UNIT = "NM"
        private const val description = "Distance"
    }
}