package com.prometheontechnologies.aviationweatherwatchface.complication.dto

import com.prometheontechnologies.aviationweatherwatchface.complication.data.WdirSerializer
import kotlinx.serialization.Serializable

/*
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
* */

@Serializable
data class APIModel(
    val metar_id: Long,
    val icaoId: String,
    val receiptTime: String,
    val obsTime: Long,
    val reportTime: String,
    val temp: Double,
    val dewp: Double,
    @Serializable(with = WdirSerializer::class)
    val wdir: Double,
    val wspd: Int,
    val wgst: Int?,
    val visib: String,
    val altim: Double,
    val slp: Double,
    val qcField: Int,
    val wxString: String?,
    @Serializable(with = WdirSerializer::class)
    val presTend: Double?,
    val maxT: Double?,
    val minT: Double?,
    val maxT24: Double?,
    val minT24: Double?,
    val precip: Double?,
    val pcp3hr: Double?,
    val pcp6hr: Double?,
    val pcp24hr: Double?,
    val snow: Double?,
    val vertVis: Int?,
    val metarType: String,
    val rawOb: String,
    val mostRecent: Int,
    val lat: Double,
    val lon: Double,
    val elev: Int,
    val prior: Int,
    val name: String,
    val clouds: List<Cloud>
)

@Serializable
data class Cloud(
    val cover: String,
    val base: Int
)