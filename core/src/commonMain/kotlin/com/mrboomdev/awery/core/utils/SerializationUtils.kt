package com.mrboomdev.awery.core.utils

import androidx.compose.runtime.State
import com.mrboomdev.awery.core.utils.collection.iterate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.text.append

fun Any?.serializeByReflection(
    rethrowGetterExceptions: Boolean = true,
    maxDepth: Int = Int.MAX_VALUE
): String = serializeByReflection(
    obj = this, 
    tabsCount = 0,
    rethrowGetterExceptions = rethrowGetterExceptions,
    maxDepth = maxDepth,
    currentDepth = 1
)

private fun serializeByReflection(
    obj: Any?,
    tabsCount: Int,
    rethrowGetterExceptions: Boolean,
    maxDepth: Int,
    currentDepth: Int
): String = when(obj) {
    is String,
    is Boolean,
    is Number,
    null -> obj.toString()
    
    is Enum<*> -> obj.name
    
    is State<*> -> serializeByReflection(
        obj = obj.value,
        tabsCount = tabsCount,
        rethrowGetterExceptions = rethrowGetterExceptions,
        maxDepth = maxDepth,
        currentDepth = currentDepth
    )

    is StateFlow<*> -> serializeByReflection(
        obj = obj.value,
        tabsCount = tabsCount,
        rethrowGetterExceptions = rethrowGetterExceptions,
        maxDepth = maxDepth,
        currentDepth = currentDepth
    )

    is List<*> -> "LISTS ARE NOT SUPPORTED YET! TODO: IMPL"

    is Map<*, *> -> "MAPS ARE NOT SUPPORTED YET! TODO: IMPL"
    
    else -> buildString {
        if(currentDepth > maxDepth) {
            return "null"
        }
        
        @Suppress("UNCHECKED_CAST")
        val clazz = obj::class as KClass<Any>
        
        if(clazz.qualifiedName?.startsWith("kotlin.Array") == true) {
            return "ARRAYS ARE NOT SUPPORTED YET! TODO: IMPL"
        }
        
        clazz.memberProperties.also { members ->
            if(members.isEmpty()) {
                return "{}"
            }
            
            append("{\n")
        }.iterate { member ->
            member.isAccessible = true
            
            append("\t".repeat(tabsCount + 1))
            append("\"")
            append(member.name)
            append("\": ")
            
            append(try {
                serializeByReflection(
                    obj = member.get(obj),
                    tabsCount = tabsCount + 1,
                    rethrowGetterExceptions = rethrowGetterExceptions,
                    maxDepth = maxDepth,
                    currentDepth = currentDepth + 1
                )
            } catch(t: Throwable) {
                if(rethrowGetterExceptions) {
                    throw t
                } else "null"
            })
            
            if(hasNext()) {
                append(",")
            }
            
            append("\n")
        }

        append("\t".repeat(tabsCount))
        append("}")
    }
}

object JavaSerializer: KSerializer<java.io.Serializable> {
    private val serializer = ByteArraySerializer()
    override val descriptor = serializer.descriptor

    override fun serialize(encoder: Encoder, value: java.io.Serializable) {
        encoder.encodeSerializableValue(serializer, value.serialize())
    }

    override fun deserialize(decoder: Decoder): java.io.Serializable {
        return decoder.decodeSerializableValue(serializer).deserialize()
    }
}

/**
 * Uses a built-in Java serialization framework.
 * @see deserialize
 */
fun java.io.Serializable.serialize(): ByteArray {
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
fun ByteArray.deserialize(): java.io.Serializable {
    val bais = ByteArrayInputStream(this)
    val ois = ObjectInputStream(bais)

    @Suppress("UNCHECKED_CAST")
    val o = ois.readObject()

    ois.close()
    return o as Serializable
}