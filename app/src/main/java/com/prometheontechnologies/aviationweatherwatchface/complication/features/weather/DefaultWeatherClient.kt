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

    private data class WindData(
        val direction: Int? = null,
        val speed: Int? = null,
        val gust: Int? = null,
        val wind: String? = null
    )

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

    // Helper functions to parse METAR data
    private fun parseTemperature(tempStr: String): Int {
        return if (tempStr.startsWith("M")) {
            -tempStr.substring(1).toIntOrNull()!!
        } else {
            tempStr.toIntOrNull()!!
        }
    }

    private fun processWindPart(windPart: String): WindData {
        val windRegex = Regex("^(\\d{3})(\\d{2})(G(\\d{2}))?KT$")
        val matchResult = windRegex.find(windPart)

        if (matchResult != null) {
            if (matchResult.value == "00000KT") return WindData(wind = "Calm")
            val (direction, speed, _, gust) = matchResult.destructured
            val windDirection = direction.toInt() // Convert to integer
            val windSpeed = speed.toInt() // Convert to integer
            val windGust = gust.toIntOrNull() // Convert to integer if present, null otherwise


            return WindData(windDirection, windSpeed, windGust)
        } else {
            throw IllegalArgumentException("Invalid wind data format")
        }
    }

    private fun parseMetar(metarString: String): Result<MetarData> {
        val parts = metarString.split(" ").filter { it.isNotEmpty() /*&& it != "AUTO"*/ }

        if (parts.size < 9) return Result.failure(WeatherClient.WeatherNotAvailableException("METAR string is too short."))

        return try {
            var windData: WindData? = null
            var visibility: String? = null
            val skyConditions = mutableListOf<String>()
            var temperatureC: Int? = null
            var dewPointC: Int? = null
            var altimeterInHg: Double? = null
            var seaLevelPressureMb: Double? = null


            val airportCode = parts[0]

            val timePart = parts.find { it.endsWith("Z") }
                ?: throw WeatherClient.WeatherNotAvailableException("Time part not found")
            if (timePart.length != 7) throw WeatherClient.WeatherNotAvailableException("Invalid time format")

            val dayOfMonth = timePart.substring(0, 2).toIntOrNull()
                ?: throw IllegalArgumentException("Invalid day of month")
            val timeZulu = timePart.substring(2, 6)

            parts.forEach { part ->
                try {
                    when {
                        part.matches(Regex("^(\\d{5}KT|\\d{5}G\\d{2}KT)$")) -> {
                            windData = processWindPart(part)
                        }

                        part.matches(Regex("^[0-9]+SM$")) -> {
                            visibility = part/*.replace("SM", "")*/
                        }

                        part.matches(Regex("^(FEW|SCT|BKN|OVC)[0-9]{3}$")) -> {
                            if (part.isEmpty()) throw WeatherClient.WeatherNotAvailableException("Sky condition not found")
                            skyConditions.add(part)
                        }

                        part.matches(Regex("^M?[0-9]{2}/M?[0-9]{2}$")) -> {
                            val tempDewSplit = part.split("/")
                            if (tempDewSplit.size != 2) throw WeatherClient.WeatherNotAvailableException(
                                "Invalid temperature/dew point format"
                            )

                            temperatureC = parseTemperature(tempDewSplit[0])
                            dewPointC = parseTemperature(tempDewSplit[1])
                        }

                        part.matches(Regex("^A[0-9]{4}$")) -> {
                            altimeterInHg = part.substring(1).toDoubleOrNull()?.div(100)
                                ?: throw WeatherClient.WeatherNotAvailableException("Invalid altimeter value")
                        }

                        part.startsWith("SLP") -> {
                            val slp = part.substring(3).toDoubleOrNull()
                                ?: throw WeatherClient.WeatherNotAvailableException("Invalid SLP value")
                            seaLevelPressureMb = slp / 10 + 1000
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    println("Error processing data: ${e.message}")
                    // Handle error or fallback
                }
            }

            if (altimeterInHg != null && (altimeterInHg!! !in 28.0..32.0)) {
                throw WeatherClient.WeatherNotAvailableException("Altimeter setting out of expected range: $altimeterInHg")
            }
            val metarData = MetarData(
                airportCode,
                dayOfMonth,
                timeZulu,
                windData?.wind,
                windData?.speed,
                windData?.direction,
                windData?.gust,
                visibility,
                skyConditions,
                temperatureC,
                dewPointC,
                altimeterInHg,
                seaLevelPressureMb
            )

            Log.v(TAG, "Parsed METAR: ${metarData.toText()}")
            Result.success(metarData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
