package com.mrboomdev.awery.core.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.builtins.ByteArraySerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

object JavaSerializer: KSerializer<Serializable> {
    private val serializer = ByteArraySerializer()
    override val descriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: Serializable) {
        encoder.encodeSerializableValue(serializer, value.serialize())
    }

    override fun deserialize(decoder: Decoder): Serializable {
        return decoder.decodeSerializableValue(serializer).deserialize()
    }
}

/**
 * Uses a built-in Java serialization framework.
 * @see deserialize
 */
fun Serializable.serialize(): ByteArray {
    return ByteArrayOutputStream().apply {
        val oos = ObjectOutputStream(this)
        oos.writeObject(this@serialize)
        oos.close()
    }.toByteArray()
}

/**
 * Uses a built-in Java serialization framework.
 * @see serialize
 */
// TODO: Add ability to specify a custom classloader so that we can serializable all external classes.
fun ByteArray.deserialize(): Serializable {
    val bais = ByteArrayInputStream(this)
    val ois = ObjectInputStream(bais)

    @Suppress("UNCHECKED_CAST")
    val o = ois.readObject()

    ois.close()
    return o as Serializable
}