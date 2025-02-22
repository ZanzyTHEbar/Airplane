package com.prometheontechnologies.aviationweatherwatchface.complication.data.dto

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.prometheontechnologies.aviationweatherwatchface.complication.features.settings.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/** SERIALIZER **/
object DataStoreSerializer : Serializer<UserPreferences> {

    override val defaultValue = UserPreferences(
        updatePeriod = 15
    )

    override suspend fun readFrom(input: InputStream): UserPreferences {
        try {
            return Json.decodeFromString(
                UserPreferences.serializer(), input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read UserPrefs", serialization)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(UserPreferences.serializer(), t)
                    .encodeToByteArray()
            )
        }
    }
}


