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
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class IDENTComplicationService : SuspendingComplicationDataSourceService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        var complicationData: ComplicationsDataStore? = null

        applicationContext
            .complicationsDataStore
            .data.catch { e ->
                Log.e(TAG, "Error getting complicationsDataStore", e)
            }.onEach { data ->
                complicationData = data.complicationsDataStore
            }.launchIn(scope)

        val text = complicationData?.ident

        return when (request.complicationType) {
            ComplicationType.SHORT_TEXT -> {
                text?.let { PlainComplicationText.Builder(it).build() }?.let {
                    ShortTextComplicationData
                        .Builder(
                            text = it,
                            contentDescription = PlainComplicationText.Builder(description).build()
                        )
                        .build()
                }
            }

            ComplicationType.LONG_TEXT -> {
                LongTextComplicationData.Builder(
                    PlainComplicationText.Builder(
                        "${description}: $text${UNIT}"
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
        private const val description = "Ident"
        private const val placeHolder = "KMIC"
        private const val UNIT = ""
    }
}