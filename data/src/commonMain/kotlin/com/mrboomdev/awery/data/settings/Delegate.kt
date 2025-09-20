@file:Suppress("unused")

package com.mrboomdev.awery.data.settings

import kotlin.reflect.KProperty

interface SettingDelegate<T, E: Setting<T>> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): E
}

fun setting(initialValue: String) = setting { StringSetting(it, initialValue) }
fun setting(initialValue: Boolean) = setting { BooleanSetting(it, initialValue) }
fun setting(initialValue: Int) = setting { IntSetting(it, initialValue) }
fun setting(initialValue: Long) = setting { LongSetting(it, initialValue) }
inline fun <reified T: Enum<T>> setting(initialValue: T) = setting { EnumSetting(it, initialValue) }

fun <T, E: Setting<T>> setting(
    factory: (key: String) -> E
) = object : SettingDelegate<T, E> {
    var cachedSetting: E? = null

    override operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        cachedSetting ?: factory(property.name).also { cachedSetting = it }
}