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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WindComplicationService : SuspendingComplicationDataSourceService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        Log.d(TAG, "Complication Activated: $complicationInstanceId")
    }

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

        var complicationData: ComplicationsDataStore? = null

        applicationContext
            .complicationsDataStore
            .data.catch { e ->
                Log.e(TAG, "Error getting complicationsDataStore", e)
            }.onEach { data ->
                complicationData = data.complicationsDataStore
            }.launchIn(scope)

        val text = "${complicationData?.windSpeed}/${complicationData?.windDirection}"

        return Utilities.presentComplicationViews(
            this,
            request.complicationType,
            description,
            text,
            R.drawable.wind_foreground
        )
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