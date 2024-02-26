package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    companion object {
        private const val BASE_URL = "https://aviationweather.gov"

        private var json: Json = Json {
            ignoreUnknownKeys = true
        }

        @OptIn(ExperimentalSerializationApi::class)
        val apiInstance: WeatherApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        HttpLoggingInterceptor()
                            .setLevel(HttpLoggingInterceptor.Level.BASIC)
                    )
                    .build()
            )
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApi::class.java)
    }

    /**
     * Get the METAR details for the given latitude and longitude
     * ?ids=KMCI&taf=true&format=json
     */
    @GET("/api/data/metar")
    suspend fun getMetarDetails(
        @Query("ids") ids: String?,
        @Query("taf") taf: Boolean?,
        @Query("format") format: String
    ): List<APIModel>

    @GET("/api/data/metar")
    suspend fun getMetarDetailsBBOX(
        @Query("taf") taf: Boolean?,
        @Query("format") format: String,
        @Query("bbox") bbox: String
    ): List<APIModel>
}