package com.prometheontechnologies.aviationweatherwatchface.complication.features.complications

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.presentComplicationViews

class FreezingLevelComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return this.presentComplicationViews(
            type,
            description,
            "2000",
            R.drawable.ic_temp
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val groundTemp = LocalDataRepository.weatherData.value?.temp ?: 0.0
        val height = calculateHeightForTemperature(groundTemp, 0.0)

        val text = "${height.toInt()}${UNIT}"

        return this.presentComplicationViews(
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
        Log.d(TAG, "Complication Deactivated: $complicationInstanceId")
        super.onComplicationDeactivated(complicationInstanceId)
    }

    private fun calculateHeightForTemperature(T_sfc: Double, T_height: Double): Double {
        // Constants
        val conversionFactor = 0.002
        // Calculating height using the given formula
        return (T_sfc - T_height) / conversionFactor
    }

    companion object {
        private val TAG = DistanceComplicationService::class.java.simpleName
        private const val NAUTICAL_MILES_CONSTANT = 1.852
        private const val UNIT = "FT"
        private const val description = "Freezing Level"
    }
}