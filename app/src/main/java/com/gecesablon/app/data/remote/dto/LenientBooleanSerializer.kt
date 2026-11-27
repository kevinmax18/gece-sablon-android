package com.gecesablon.app.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Serializer fleksibel untuk menangani nilai Boolean dari backend Laravel yang bisa berupa:
 * - Boolean asli: true, false
 * - Angka integer: 1 (true), 0 (false)
 * - String: "1", "0", "true", "false", case-insensitive
 * - Fallback aman: false jika format tidak dikenali atau null
 */
object LenientBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LenientBooleanSerializer", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean {
        if (decoder is JsonDecoder) {
            val element = decoder.decodeJsonElement()
            if (element is JsonPrimitive) {
                // 1. Coba decode boolean native
                element.booleanOrNull?.let { return it }

                // 2. Coba decode integer (1 / 0)
                element.intOrNull?.let { return it != 0 }

                // 3. Coba decode dari isi string
                val str = element.content.trim().lowercase()
                return when (str) {
                    "1", "true" -> true
                    "0", "false", "" -> false
                    else -> false
                }
            }
        }
        return try {
            decoder.decodeBoolean()
        } catch (_: Exception) {
            false
        }
    }

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeBoolean(value)
    }
}
