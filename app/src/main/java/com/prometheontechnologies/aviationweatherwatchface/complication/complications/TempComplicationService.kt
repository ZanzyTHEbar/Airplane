package com.prometheontechnologies.aviationweatherwatchface.complication.complications


import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.Utilities
import com.prometheontechnologies.aviationweatherwatchface.complication.data.LocalDataRepository

class TempComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return Utilities.presentComplicationViews(
            this,
            type,
            description,
            placeHolder,
            R.drawable.ic_temp
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        /*val dataStore =
            applicationContext.complicationsDataStore.data.first().weatherServiceDataStore*/

        val dataStore = LocalDataRepository.weatherData.value

        val temp =
            dataStore?.temp ?: 0.0

        val dewPoint =
            dataStore?.dewPt ?: 0.0

        val text = "${temp}/${dewPoint}${UNIT}"

        return Utilities.presentComplicationViews(
            this,
            request.complicationType,
            description,
            text,
            R.drawable.ic_temp
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
        private const val UNIT = "℃"
        private const val description = "Temperature"
        private const val placeHolder = "23/15"
    }
}