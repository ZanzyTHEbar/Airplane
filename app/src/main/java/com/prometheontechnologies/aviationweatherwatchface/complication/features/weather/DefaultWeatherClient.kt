package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather

import android.content.Context
import android.util.Log
import com.prometheontechnologies.aviationweatherwatchface.complication.data.database.LocalDataRepository
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.MetarData
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.WeatherAPIModel
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api.WeatherApi
import com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.dto.WeatherClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DefaultWeatherClient(private val context: Context) : WeatherClient {

    companion object {
        private val TAG = DefaultWeatherClient::class.java.simpleName
    }

    override suspend fun callWeatherApi(ident: String): Result<WeatherAPIModel> {
        val weatherDTO: List<WeatherAPIModel> = WeatherApi.apiInstance.getMetarDetails(
            ident, true, "json"
        )

        if (weatherDTO.isEmpty()) {
            val message =
                "Weather API Client received a null response, trying the next closest airport."

            Log.e(TAG, message)

            // TODO: Setup notifications for weather

            return Result.failure(WeatherClient.WeatherNotAvailableException(message))
        }

        Log.d(TAG, "Weather DTO: $weatherDTO")

        // TODO: Setup logic to handle multiple weather data
        return Result.success(weatherDTO.first())
    }

    override fun getWeatherUpdates(ident: String): Flow<WeatherAPIModel> {
        return callbackFlow {
            val result = callWeatherApi(ident)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                val metarData = parseMetar(data.rawOb).getOrThrow()
                LocalDataRepository.updateMetarData(metarData)
                Log.d(TAG, "Weather updates flow sent: $data")
                trySend(data).isSuccess
            }

            awaitClose {
                Log.d(TAG, "Weather updates flow closed.")
            }
        }
    }

    private fun parseMetar(metarString: String): Result<MetarData> {
        val parts = metarString.split(" ").filter { it.isNotEmpty() }

        if (parts.size < 9) return Result.failure(WeatherClient.WeatherNotAvailableException("METAR string is too short."))

        return try {
            val airportCode = parts[0]

            /*val dayOfMonth = parts[1].substring(0, 2).toInt().takeIf { it in 1..31 }
                ?: throw WeatherClient.WeatherNotAvailableException("Invalid day of month")*/
            /* val timeZulu = parts[1].substring(2).takeIf { it.length == 4 }
                ?: throw WeatherClient.WeatherNotAvailableException("Invalid time format")*/

            val timePart = parts.find { it.endsWith("Z") }
                ?: throw WeatherClient.WeatherNotAvailableException("Time part not found")

            // Validate the format further if necessary
            if (timePart.length != 7) throw WeatherClient.WeatherNotAvailableException("Invalid time format")

            val dayOfMonth = timePart.substring(0, 2).toIntOrNull()
                ?: throw IllegalArgumentException("Invalid day of month")
            val timeZulu = timePart.substring(2, 6) // No need to convert to Int

            val wind = parts[2]
            val visibility = parts[3]

            val skyConditionIndices =
                parts
                    .indices
                    .filter { i ->
                        parts[i]
                            .matches(Regex("^(FEW|SCT|BKN|OVC)[0-9]{3}$"))
                    }
            val skyCondition = skyConditionIndices.map { parts[it] }

            val tempDew = parts[6].split("/")
            if (tempDew.size != 2) throw WeatherClient.WeatherNotAvailableException("Invalid temperature/dew point format")
            val temperatureC = tempDew[0].toInt()
            val dewPointC = tempDew[1].toInt()

            // Assuming parts[7] contains the altimeter setting starting with 'A'
            val altimeterSettingPart = parts.find { it.startsWith("A") }
                ?: throw WeatherClient.WeatherNotAvailableException("Altimeter setting not found")

            // Extract the numeric part and convert to the actual altimeter setting
            val altimeterInHg = altimeterSettingPart.substring(1).toDoubleOrNull()?.div(100)
                ?: throw WeatherClient.WeatherNotAvailableException("Invalid altimeter setting format")

            // Validate the altimeter setting
            if (altimeterInHg !in 28.0..32.0) throw WeatherClient.WeatherNotAvailableException("Altimeter setting out of expected range: $altimeterInHg")

            /*val altimeterInHg = parts[7].substring(1).toDouble().takeIf { it in 28.0..32.0 }
                ?: throw WeatherClient.WeatherNotAvailableException("Invalid altimeter setting")*/

            val slpIndex = parts.indexOfFirst { it.startsWith("SLP") }
            val seaLevelPressureMb = if (slpIndex != -1) {
                val slp =
                    parts[slpIndex].substring(3).toDoubleOrNull()
                        ?: throw WeatherClient.WeatherNotAvailableException(
                            "Invalid SLP value"
                        )
                slp / 10 + 1000
            } else 0.0

            Result.success(
                MetarData(
                    airportCode,
                    dayOfMonth,
                    timeZulu,
                    wind,
                    visibility,
                    skyCondition,
                    temperatureC,
                    dewPointC,
                    altimeterInHg,
                    seaLevelPressureMb
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
