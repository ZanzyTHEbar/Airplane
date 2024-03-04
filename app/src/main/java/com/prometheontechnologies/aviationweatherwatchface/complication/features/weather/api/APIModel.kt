package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api

import kotlinx.serialization.Serializable

/**
 * This is the model class for the API
 * @description: This is an example of a response from the weather API
 * @example:
 * [
{
"metar_id": 494866151,
"icaoId": "KMCI",
"receiptTime": "2024-02-09 19:56:24",
"obsTime": 1707508380,
"reportTime": "2024-02-09 20:00:00",
"temp": 13.9,
"dewp": 1.1,
"wdir": 50,
"wspd": 7,
"wgst": null,
"visib": "10+",
"altim": 1008.9,
"slp": 1008.7,
"qcField": 4,
"wxString": null,
"presTend": null,
"maxT": null,
"minT": null,
"maxT24": null,
"minT24": null,
"precip": null,
"pcp3hr": null,
"pcp6hr": null,
"pcp24hr": null,
"snow": null,
"vertVis": null,
"metarType": "METAR",
"rawOb": "KMCI 091953Z 05007KT 10SM FEW100 FEW250 14/01 A2979 RMK AO2 SLP087 T01390011",
"mostRecent": 1,
"lat": 39.2975,
"lon": -94.7309,
"elev": 308,
"prior": 1,
"name": "Kansas City Intl, MO, US",
"clouds": [
{
"cover": "FEW",
"base": 10000
},
{
"cover": "FEW",
"base": 25000
}
],
"rawTaf": "KMCI 091742Z 0918/1018 04007KT P6SM FEW250 FM100200 35012G18KT P6SM BKN100 FM101300 35008KT P6SM SCT025"
}
]
 **/

@Serializable
data class APIModel(
    val metar_id: Long,
    @Serializable(with = StringSerializer::class)
    val icaoId: String,
    @Serializable(with = StringSerializer::class)
    val receiptTime: String,
    val obsTime: Long,
    @Serializable(with = StringSerializer::class)
    val reportTime: String,
    @Serializable(with = DoubleSerializer::class)
    val temp: Double,
    @Serializable(with = DoubleSerializer::class)
    val dewp: Double,
    @Serializable(with = DoubleSerializer::class)
    val wdir: Double,
    @Serializable(with = IntSerializer::class)
    val wspd: Int,
    @Serializable(with = IntSerializer::class)
    val wgst: Int?,
    @Serializable(with = StringSerializer::class)
    val visib: String,
    @Serializable(with = DoubleSerializer::class)
    val altim: Double,
    @Serializable(with = DoubleSerializer::class)
    val slp: Double?,
    @Serializable(with = IntSerializer::class)
    val qcField: Int,
    @Serializable(with = StringSerializer::class)
    val wxString: String?,
    @Serializable(with = DoubleSerializer::class)
    val presTend: Double?,
    @Serializable(with = DoubleSerializer::class)
    val maxT: Double?,
    @Serializable(with = DoubleSerializer::class)
    val minT: Double?,
    @Serializable(with = DoubleSerializer::class)
    val maxT24: Double?,
    @Serializable(with = DoubleSerializer::class)
    val minT24: Double?,
    @Serializable(with = DoubleSerializer::class)
    val precip: Double?,
    @Serializable(with = DoubleSerializer::class)
    val pcp3hr: Double?,
    @Serializable(with = DoubleSerializer::class)
    val pcp6hr: Double?,
    @Serializable(with = DoubleSerializer::class)
    val pcp24hr: Double?,
    @Serializable(with = DoubleSerializer::class)
    val snow: Double?,
    @Serializable(with = IntSerializer::class)
    val vertVis: Int?,
    @Serializable(with = StringSerializer::class)
    val metarType: String,
    @Serializable(with = StringSerializer::class)
    val rawOb: String,
    @Serializable(with = IntSerializer::class)
    val mostRecent: Int,
    @Serializable(with = DoubleSerializer::class)
    val lat: Double,
    @Serializable(with = DoubleSerializer::class)
    val lon: Double,
    @Serializable(with = IntSerializer::class)
    val elev: Int,
    @Serializable(with = IntSerializer::class)
    val prior: Int,
    @Serializable(with = StringSerializer::class)
    val name: String,
    val clouds: List<Cloud>
)

@Serializable
data class Cloud(
    @Serializable(with = StringSerializer::class)
    val cover: String,
    @Serializable(with = IntSerializer::class)
    val base: Int
)