package com.prometheontechnologies.aviationweatherwatchface.complication.features.weather.api

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

object DoubleSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DoubleSerializer", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: Decoder): Double {
        // Obtain the decoder as JsonDecoder and then decode the JsonElement
        //return decoder.decodeDouble()
        return when (val element = (decoder as? JsonDecoder)?.decodeJsonElement()) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content.toDoubleOrNull() ?: 0.0
                    element.intOrNull != null -> element.int.toDouble()
                    else -> element.content.toDoubleOrNull() ?: 0.0
                }
            }

            else -> 0.0
        }
    }
}

object IntSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IntSerializer", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int {
        // Obtain the decoder as JsonDecoder and then decode the JsonElement
        return when (val element = (decoder as? JsonDecoder)?.decodeJsonElement()) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content.toIntOrNull() ?: 0
                    element.doubleOrNull != null -> element.double.toInt()
                    else -> element.content.toIntOrNull() ?: 0
                }
            }

            else -> 0
        }
    }
}

object StringSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        // Obtain the decoder as JsonDecoder and then decode the JsonElement
        return when (val element = (decoder as? JsonDecoder)?.decodeJsonElement()) {
            is JsonPrimitive -> {
                when {
                    element.doubleOrNull != null -> element.double.toString()
                    element.intOrNull != null -> element.int.toString()
                    else -> element.content
                }
            }

            else -> ""
        }
    }
}
