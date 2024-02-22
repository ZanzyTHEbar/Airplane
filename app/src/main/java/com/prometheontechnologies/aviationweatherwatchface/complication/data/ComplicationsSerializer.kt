package com.prometheontechnologies.aviationweatherwatchface.complication.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.prometheontechnologies.aviationweatherwatchface.complication.dto.ComplicationsSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
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
