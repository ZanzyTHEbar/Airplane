package com.prometheontechnologies.aviationweatherwatchface.complication.features.complications

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.presentComplicationViews

class PressureComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return this.presentComplicationViews(
            type,
            description,
            placeHolder,
            R.drawable.ic_pressure_foreground
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val pressure = LocalDataRepository.metarData.value?.altimeterInHg ?: 0.0

        val text = "$pressure$UNIT"

        return this.presentComplicationViews(
            request.complicationType,
            description,
            text,
            R.drawable.ic_pressure_foreground
        )
    }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        super.onComplicationActivated(complicationInstanceId, type)
        Log.d(TAG, "Complication Activated: $complicationInstanceId")
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
        Log.d(TAG, "Complication Deactivated: $complicationInstanceId")
    }

    companion object {
        private val TAG = PressureComplicationService::class.java.simpleName
        private const val UNIT = "inHg"
        private const val description = "Pressure"
        private const val placeHolder = "29.92"
    }
}