package com.mrboomdev.awery.core.utils

import kotlin.reflect.KClass
import kotlin.jvm.java

fun <T: Enum<T>> String.toEnumOrNull(clazz: KClass<out T>): T? {
    return try {
        java.lang.Enum.valueOf(clazz.java as Class<T>, this)
    } catch(_: IllegalArgumentException) {
        null
    }
}

fun <T: Enum<T>> String.toEnum(clazz: KClass<T>) = toEnumOrNull(clazz)!!