
package com.example.android.wearable.composeforwearos.utilities
/*
import android.content.Context
import android.util.Log
import com.example.android.wearable.composeforwearos.R
import com.example.android.wearable.composeforwearos.data.Airport
import com.example.android.wearable.composeforwearos.data.AirportDAO
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset


class AirportsDataBaseCSVLoader(private val appContext: Context, private val dao: AirportDAO) {

    private var alreadyLoaded = false

    suspend fun parseCSV(): Boolean {

        if (alreadyLoaded) {
            return true
        }

        val resourceFileInputStream =
            appContext.resources.openRawResource(R.raw.airports)

        val airports = mutableListOf<Airport>()
        var reader: BufferedReader? = null
        var csvReader: CSVReader? = null
        var nextLine: Array<String>? = null
        try {
            reader =
                BufferedReader(InputStreamReader(resourceFileInputStream, Charset.forName("UTF-8")))
            csvReader = CSVReader(reader)
            var isHeader = true

            while (csvReader.readNext().also { nextLine = it } != null) {
                if (isHeader) {
                    // Skip the header row
                    isHeader = false
                    continue
                }

                nextLine?.let {
                    if (it.size == 18) { // Ensure there are enough columns
                        val id = it[0].toInt()
                        val ident = it[1]
                        val type = it[2]
                        val name = it[3]
                        val latitudeDeg = it[4].toDouble()
                        val longitudeDeg = it[5].toDouble()
                        val elevationFt = it[6].toDoubleOrNull()
                        val continent = it[7]
                        val isoCountry = it[8]
                        val isoRegion = it[9]
                        val municipality = it[10]
                        val scheduledService = it[11]
                        val gpsCode = it[12]
                        val iataCode = it[13]
                        val localCode = it[14]
                        val homeLink = it[15]
                        val wikipediaLink = it[16]
                        val keywords = it[17]

                        val airport = Airport(
                            id,
                            ident,
                            type,
                            name,
                            latitudeDeg,
                            longitudeDeg,
                            elevationFt,
                            continent,
                            isoCountry,
                            isoRegion,
                            municipality,
                            scheduledService,
                            gpsCode,
                            iataCode,
                            localCode,
                            homeLink,
                            wikipediaLink,
                            keywords
                        )
                        airports.add(airport)
                    }
                }
            }

            if (airports.isEmpty()) {
                return false
            }

            // Log the first few airports for demonstration
            airports.take(5).forEach { airport ->
                Log.d(
                    TAG,
                    "Airport: Name: ${airport.name}, ICAO: ${airport.icao}, Latitude: ${airport.latitude}, Longitude: ${airport.longitude}"
                )
            }

            airports.forEach { airport ->
                if (airport.name.isBlank() || airport.icao.isBlank() || airport.latitudeDeg == 0.0 || airport.longitudeDeg == 0.0) {
                    // Skip airports with missing data
                    return@forEach
                }

                val newAirPort = com.example.android.wearable.composeforwearos.data.Airport(
                    name = airport.name,
                    icao = airport.icao,
                    latitude = airport.latitude,
                    longitude = airport.longitude
                )
                Log.d(
                    TAG,
                    "Inserting airport: ${newAirPort.name}, ICAO: ${newAirPort.icao}, Latitude: ${newAirPort.latitude}, Longitude: ${
                        newAirPort.longitude
                    }"
                )
                dao.insert(newAirPort)
            }

            alreadyLoaded = true
            return true
        } catch (e: Exception) {
            // Handle possible exceptions
            e.printStackTrace()

            val size = nextLine?.size ?: 0
            val end = if (size > 0) {
                nextLine?.elementAt(size - 1)
            } else {
                "No data found"
            }

            Log.wtf(TAG, "Error: $end ${e.message}")
            return false
        } finally {
            // Ensure resources are closed
            try {
                csvReader?.close()
                reader?.close()
            } catch (e: Exception) {
                // Handle possible close exceptions
                e.printStackTrace()
                Log.e(TAG, "Error: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "AirportsDataBaseCSVLoader"
    }
}*/
