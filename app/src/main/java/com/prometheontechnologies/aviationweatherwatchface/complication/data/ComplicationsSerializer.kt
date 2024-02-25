package com.prometheontechnologies.aviationweatherwatchface.complication.data

import android.location.Location
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.io.InputStream
import java.io.OutputStream

/** SERIALIZER **/
object ComplicationsDataSerializer : Serializer<ComplicationsSettingsStore> {

    override val defaultValue = ComplicationsSettingsStore()
    override suspend fun readFrom(input: InputStream): ComplicationsSettingsStore {
        try {
            return Json.decodeFromString(
                ComplicationsSettingsStore.serializer(), input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read UserPrefs", serialization)
        }
    }

    override suspend fun writeTo(t: ComplicationsSettingsStore, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(ComplicationsSettingsStore.serializer(), t)
                    .encodeToByteArray()
            )
        }
    }
}

object LocationSerializer : KSerializer<Location> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Location") {
        element<Double>("latitude")
        element<Double>("longitude")
        element<String>("provider")
    }

    override fun serialize(encoder: Encoder, value: Location) {
        // Create a JSON object representation of the Location
        val locationJson = JsonObject(buildMap {
            put("latitude", JsonPrimitive(value.latitude))
            put("longitude", JsonPrimitive(value.longitude))
            put("provider", JsonPrimitive(value.provider))
        })

        // Use JsonEncoder to encode the JSON object
        if (encoder is JsonEncoder) {
            encoder.encodeJsonElement(locationJson)
        }
    }

    override fun deserialize(decoder: Decoder): Location {
        // Decode the JSON object
        val jsonObject = (decoder as? JsonDecoder)?.decodeJsonElement() as? JsonObject
        val provider = jsonObject?.get("provider")?.jsonPrimitive?.content ?: ""

        // Create a new Location instance with the decoded provider
        val location = Location(provider)

        jsonObject?.let {
            val latitude = it["latitude"]?.jsonPrimitive?.doubleOrNull
            val longitude = it["longitude"]?.jsonPrimitive?.doubleOrNull

            if (latitude != null && longitude != null) {
                location.latitude = latitude
                location.longitude = longitude
            }
        }
        return location
    }
}

