package com.prometheontechnologies.aviationweatherwatchface.complication.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    companion object {
        private const val BASE_URL = "https://aviationweather.gov"


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
            .addConverterFactory(GsonConverterFactory.create())
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

    //@GET("/taf")
    //suspend fun getTafDetails(@Query("lat") lat: Double,@Query("lon") long: Double,@Query("appid") appid: String): WeatherDTO
}