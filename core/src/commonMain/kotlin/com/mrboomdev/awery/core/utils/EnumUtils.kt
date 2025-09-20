package com.mrboomdev.awery.core.utils

import kotlin.enums.enumEntries
import kotlin.jvm.java
import kotlin.reflect.KClass

/**
 * Returns next element of the enum or the first if this is the last one.
 */
inline fun <reified T: Enum<T>> T.next(): T {
    val entries = enumEntries<T>()
    val index = entries.indexOf(this)
    return entries[if(index > entries.size - 2) 0 else index + 1]
}

/**
 * Converts this string to an enum constant of the specified enum type.
 * Returns null if the string does not match any of the declared enum constants of the specified enum type
 * or if the specified enum type has no enum constants.
 *
 * @param clazz The class of the enum type.
 * @return The enum constant, or null if the string does not match any of the declared enum constants.
 */
fun <T: Enum<T>> String.toEnumOrNull(clazz: KClass<out T>): T? {
    return try {
        java.lang.Enum.valueOf(clazz.java as Class<T>, this)
    } catch(_: IllegalArgumentException) {
        null
    }
}

/**
 * Converts a string to an enum of the specified type.
 *
 * @param clazz The class of the enum to convert to.
 * @return The enum constant corresponding to this string.
 * @throws IllegalArgumentException if the specified enum type has no constant with the specified name,
 * or the specified class object does not represent an enum type.
 */
fun <T: Enum<T>> String.toEnum(clazz: KClass<T>) =
    java.lang.Enum.valueOf(clazz.java as Class<T>, this)