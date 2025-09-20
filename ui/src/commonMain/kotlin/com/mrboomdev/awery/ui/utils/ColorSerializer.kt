package com.mrboomdev.awery.ui.utils

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
private data class SerializableColor(
    val a: Float, val r: Float, val g: Float, val b: Float
)

object ColorSerializer: KSerializer<Color> {
    override val descriptor = SerializableColor.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Color
    ) {
        encoder.encodeSerializableValue(
            SerializableColor.serializer(),
            SerializableColor(value.alpha, value.red, value.green, value.blue))
    }

    override fun deserialize(decoder: Decoder): Color {
        val color = decoder.decodeSerializableValue(SerializableColor.serializer())
        return Color(color.r, color.g, color.b, color.a)
    }
}