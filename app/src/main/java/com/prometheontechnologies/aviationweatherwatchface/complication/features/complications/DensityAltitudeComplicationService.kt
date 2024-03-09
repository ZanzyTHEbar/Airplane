package com.prometheontechnologies.aviationweatherwatchface.complication.features.complications

import android.util.Log
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.prometheontechnologies.aviationweatherwatchface.complication.R
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.utils.presentComplicationViews
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.round

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

        val inFeet = DensityAltitudeHelper.m2ft(result)

        val text = "${inFeet.toInt()}${UNIT}"

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
        val airportTempCelsius = LocalDataRepository.metarData.value?.temperatureC ?: 0
        val airportDewP = LocalDataRepository.metarData.value?.dewPointC ?: 0
        val airportPressureInHg = LocalDataRepository.metarData.value?.altimeterInHg
            ?: 29.92 // Assuming standard pressure at sea level if not available
        val airportElevationFeet =
            LocalDataRepository.weatherData.value?.elev
                ?: 0 // Assuming this is in feet for simplicity

        // Convert pressure from inHg to hPa for internal calculations
        val airportPressureHpa = airportPressureInHg * 33.8639

        // Assuming isDryAir and dewptRaw could be dynamically determined;
        // for simplicity in this example, assuming not dry air and no dew point specified
        val isDryAir = false

        val result = DensityAltitudeHelper.calcDenAlt(
            altRaw = airportElevationFeet.toDouble(),
            tempRaw = airportTempCelsius.toDouble(),
            qnhRaw = airportPressureHpa,
            isDryAir = isDryAir,
            dewptRaw = airportDewP.toDouble(),
            altUnitSelectedIndex = 0, // Not needed after simplification, kept for function signature
            tempUnitSelectedIndex = 0, // Not needed after simplification, kept for function signature
            qnhUnitSelectedIndex = 0  // Not needed after simplification, kept for function signature
        )

        return Result.success(result)
    }

    companion object {
        private val TAG = DensityAltitudeComplicationService::class.java.simpleName
        private const val NAUTICAL_MILES_CONSTANT = 1.852
        private const val UNIT = "FT"
        private const val description = "Density Altitude"
    }
}


object DensityAltitudeHelper {

    val TAG: String = DensityAltitudeHelper::class.java.simpleName

    // Conversion from meters to feet
    fun m2ft(m: Double): Double = m / 0.3048

    // Conversion from feet to meters
    private fun ft2m(feet: Double): Double = feet * 0.3048

    // Conversion from degrees Celsius to Kelvin
    fun degC2K(TC: Double): Double = TC + 273.15

    // Conversion from Kelvin to degrees Celsius
    private fun degK2C(TK: Double): Double = TK - 273.15

    // Conversion from millibars to inches of Mercury
    fun mb2inHg(mb: Double): Double = mb / 33.86388158

    // Rounds a number to a specified number of decimal places
    private fun rounder(number: Double, decimal: Int): Double =
        round(number * 10.0.pow(decimal)) / 10.0.pow(decimal)

    // Calculates Relative Humidity
    fun calcRH(
        TK: Double,
        dewptK: Double,
        beta: Double = 17.625,
        lbda: Double = 243.04,
        decimals: Int = 2
    ): Double {
        val TC = degK2C(TK)
        val dewpt = degK2C(dewptK)
        val rh = (exp((beta * dewpt) / (lbda + dewpt)) / exp((beta * TC) / (lbda + TC))) * 100
        return rounder(rh, decimals)
    }

    // Calculates Saturation Vapour Pressure
    private fun satVapourPres(TK: Double): Double {
        val TC = degK2C(TK)
        val eso = 6.1078
        val c = doubleArrayOf(
            0.99999683,
            -0.90826951E-02,
            0.78736169E-04,
            -0.61117958E-06,
            0.43884187E-08,
            -0.29883885E-10,
            0.21874425E-12,
            -0.17892321E-14,
            0.11112018E-16,
            -0.30994571E-19
        )
        val poly = c.indices.sumOf { c[it] * TC.pow(it) }
        return eso / poly.pow(8)
    }

    // Converts geometric altitude to geopotential altitude
    private fun geometric2Potential(altm: Double): Double {
        val E = 6356.766
        val Z = altm / 1000
        val H = Z * E / (E + Z)
        return H * 1000
    }

    // Converts geopotential altitude to geometric altitude
    private fun potential2Geometric(h: Double): Double {
        val E = 6356.766
        val H = h / 1000
        val Z = E * H / (E - H)
        return Z * 1000
    }

    // Calculates absolute air pressure
    private fun absPressure(QNHmb: Double, altm: Double): Double {
        val k1 = 0.190263
        val k2 = 8.417286e-5
        val h = geometric2Potential(altm)
        return (QNHmb.pow(k1) - k2 * h).pow(1 / k1)
    }

    // Calculates dry air pressure
    private fun dryAirPressure(altm: Double, QNHmb: Double, dewptK: Double): Double {
        val P = absPressure(QNHmb, altm)
        val Pv = satVapourPres(dewptK)
        return P - Pv
    }

    // Calculates air density
    private fun airDensity(TK: Double, Pd: Double, Pv: Double): Double {
        val Rd = 287.05
        val Rv = 461.495
        val pd = Pd * 100
        val pv = Pv * 100
        return (pd / (Rd * TK)) + (pv / (Rv * TK))
    }

    // Calculates density altitude in SI units
    private fun DenAltSI(rho: Double): Double {
        val g = 9.80665
        val P0 = 101325
        val T0 = 288.15
        val L = 6.5
        val R = 8.314320
        val M = 28.9644
        val H = (T0 / L) * (1 - (1000 * R * T0 * rho / (M * P0)).pow((L * R) / (g * M - L * R)))
        return potential2Geometric(H * 1000)
    }

    // Calculates pressure altitude in SI units
    fun presAltitudeSI(altm: Double, QNHmb: Double): Double {
        val pSL = 1013.25
        val deltapresSI = ft2m(145366.45 * (1 - QNHmb / pSL).pow(0.190284))
        return altm + deltapresSI
    }

    fun calcDenAlt(
        altRaw: Double,
        tempRaw: Double,
        qnhRaw: Double,
        isDryAir: Boolean,
        dewptRaw: Double?,
        altUnitSelectedIndex: Int,
        tempUnitSelectedIndex: Int,
        qnhUnitSelectedIndex: Int
    ): Double {
        val altUnit = if (altUnitSelectedIndex == 0) 0.3048 else 1.0
        val tempUnit = if (tempUnitSelectedIndex == 0) 1 else 3
        val qnhUnit = if (qnhUnitSelectedIndex == 0) 1.0 else 33.86388158

        val QNHmb = qnhRaw * qnhUnit
        val altSI = altRaw * altUnit
        val tempRawSI = when (tempUnitSelectedIndex) {
            0 -> tempRaw + 273.15
            else -> (tempRaw + 459.67) * (5.0 / 9.0)
        }
        val dewptSI = when {
            isDryAir -> -99999.0 // Dry air approximation
            else -> dewptRaw?.let {
                when (tempUnitSelectedIndex) {
                    0 -> it + 273.15
                    else -> (it + 459.67) * (5.0 / 9.0)
                }
            } ?: Double.NaN
        }

        // Perform calculations
        val Pv = satVapourPres(dewptSI)
        val Pd = dryAirPressure(altSI, QNHmb, dewptSI)
        val rho = airDensity(tempRawSI, Pd, Pv)
        val denAltSI = DenAltSI(rho)

        // Output results (assuming a console application or debug output)
        Log.d(TAG, "Density Altitude: $denAltSI meters")
        return denAltSI
    }
}