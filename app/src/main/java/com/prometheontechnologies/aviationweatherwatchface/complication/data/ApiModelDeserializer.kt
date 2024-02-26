package com.prometheontechnologies.aviationweatherwatchface.complication.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull

object WdirSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("WspdSerializer", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double) {
        encoder.encodeDouble(value)
    }

    override fun deserialize(decoder: Decoder): Double {
        // Obtain the decoder as JsonDecoder and then decode the JsonElement
        return when (val element = (decoder as? JsonDecoder)?.decodeJsonElement()) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content.toDoubleOrNull() ?: 0.0
                    element.intOrNull != null -> element.int.toDouble()
                    else -> 0.0
                }
            }

            else -> 0.0
        }
    }
}
