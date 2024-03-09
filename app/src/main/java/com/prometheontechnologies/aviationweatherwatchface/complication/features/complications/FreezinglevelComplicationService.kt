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
        val height = calculateHeightForTemperature(groundTemp)

        if (height.isNaN()) return null

        val text = if (height.toInt() <= 0) {
            "Surface"
        } else {
            "${height.toInt()}${UNIT}"
        }

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

    private fun calculateHeightForTemperature(T_sfc: Double, T_freezing: Double = 0.0): Double {
        // Constants
        val lapseRate = 0.0065 // Temperature decrease in °C per meter
        val metersToFeet = 3.28084 // Conversion factor from meters to feet

        // Calculating height difference required for temperature to decrease to the freezing point in meters
        val heightInMeters = (T_sfc - T_freezing) / lapseRate

        // Convert height from meters to feet
        val heightInFeet = heightInMeters * metersToFeet

        return heightInFeet
    }

    companion object {
        private val TAG = FreezingLevelComplicationService::class.java.simpleName
        private const val NAUTICAL_MILES_CONSTANT = 1.852
        private const val UNIT = "FT"
        private const val description = "Freezing Level"
    }
}