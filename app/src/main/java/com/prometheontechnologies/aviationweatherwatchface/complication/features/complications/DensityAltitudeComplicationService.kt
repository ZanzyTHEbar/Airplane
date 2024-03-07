package com.prometheontechnologies.aviationweatherwatchface.complication.features.complications

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.presentComplicationViews

class DensityAltitudeComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return this.presentComplicationViews(
            type,
            description,
            "2000",
            R.drawable.ic_flight_takoff_foreground
        )
    }

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
        Log.d(TAG, "onComplicationRequest() id: ${request.complicationInstanceId}")

        val result = calculateDensityAltitude().getOrNull() ?: return null
        val text = "${result.toInt()}${UNIT}"

        return this.presentComplicationViews(
            request.complicationType,
            description,
            text,
            R.drawable.ic_flight_takoff_foreground
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

    private fun calculateDensityAltitude(): Result<Double> {
        val airportTemp = LocalDataRepository.metarData.value?.temperatureC ?: 0
        val airportPressure = LocalDataRepository.metarData.value?.altimeterInHg ?: 0.0
        val airportElevation = LocalDataRepository.weatherData.value?.elev ?: 0
        val temperatureFahrenheit = (airportTemp * 9 / 5) + 32

        // Calculate Pressure Altitude (simplified, assuming sea level standard pressure of 29.92 inHg)
        // Calculate Pressure Altitude in feet using the altimeter setting
        val pressureAltitudeFeet = (29.92 - airportPressure) * 1000 + airportElevation
        val isaTemp = calculateISATemperature(airportElevation.toDouble())

        // Convert ISA temperature to Fahrenheit
        val isaTempFahrenheit = (isaTemp * 9 / 5) + 32

        // Calculate Density Altitude

        /*(29.92 - pressureAltitudeFeet) * (1000 + 444 * airportElevation) +
                (120 * (1.1 * temperatureFahrenheit - (15 - 0.444 * airportElevation / 1000)))*/
        return Result.success(pressureAltitudeFeet + (120 * (temperatureFahrenheit - isaTempFahrenheit)))
    }

    private fun calculateISATemperature(elevationMeters: Double): Double {
        val seaLevelTemperatureC = 15.0 // ISA sea level standard temperature in Celsius
        val temperatureLapseRateCPerMeter = -0.0065 // Temperature lapse rate in Celsius per meter

        // Calculate the ISA temperature based on the elevation
        return seaLevelTemperatureC + (temperatureLapseRateCPerMeter * elevationMeters)
    }

    companion object {
        private val TAG = DensityAltitudeComplicationService::class.java.simpleName
        private const val NAUTICAL_MILES_CONSTANT = 1.852
        private const val UNIT = "FT"
        private const val description = "Density Altitude"
    }
}