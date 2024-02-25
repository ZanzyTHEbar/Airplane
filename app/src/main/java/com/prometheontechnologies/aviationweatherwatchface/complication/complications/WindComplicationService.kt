package com.prometheontechnologies.aviationweatherwatchface.complication.complications

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.data.complicationsDataStore
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsDataStore
import kotlinx.coroutines.flow.first

class WindComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return Utilities.presentComplicationViews(
            this,
            type,
            description,
            placeHolder,
            R.drawable.wind_foreground
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val complicationData: ComplicationsDataStore =
            applicationContext.complicationsDataStore.data.first().complicationsDataStore

        val text = "${complicationData.windSpeed}nts/${complicationData.windDirection}°"

        return Utilities.presentComplicationViews(
            this,
            request.complicationType,
            description,
            text,
            R.drawable.wind_foreground
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
        private val TAG = TempComplicationService::class.java.simpleName
        private const val description = "Wind"
        private const val placeHolder = "23/15"
    }
}