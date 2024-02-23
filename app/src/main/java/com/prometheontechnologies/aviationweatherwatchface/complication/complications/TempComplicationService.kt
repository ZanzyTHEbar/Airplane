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

class TempComplicationService : SuspendingComplicationDataSourceService() {

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

        val text = "${complicationsDataStore.temperature}/${complicationsDataStore.dewPoint}"

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
        private const val UNIT = "°C"
        private const val description = "Temperature"
        private const val placeHolder = "23/15"
    }
}