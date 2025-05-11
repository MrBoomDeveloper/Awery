package com.mrboomdev.awery.data.settings

import androidx.compose.runtime.IntState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.mrboomdev.awery.core.utils.toEnumOrNull
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.set

internal expect fun platformSettings(): ObservableSettings

@PublishedApi
internal val settingsImpl by lazy { platformSettings() }

sealed interface Setting<T> {
    val key: String
    val initialValue: T
    var value: T
    val state: State<T>
}

class EnumSetting<T: Enum<T>>(
    override val key: String,
    override val initialValue: T
): Setting<T> {
    private val _state = mutableStateOf(value)
    override val state: State<T> = _state

    override var value: T
        get() = settingsImpl.getString(key, initialValue.name).toEnumOrNull(initialValue::class) ?: initialValue
        set(value) {
            settingsImpl[key] = value.name
            _state.value = value
        }
}

class StringSetting(
    override val key: String,
    override val initialValue: String
): Setting<String> {
    private val _state = mutableStateOf(value)
    override val state: State<String> = _state

    override var value: String
        get() = settingsImpl.getString(key, initialValue)
        set(value) {
            settingsImpl[key] = value
            _state.value = value
        }
}

class BooleanSetting(
    override val key: String,
    override val initialValue: Boolean
): Setting<Boolean> {
    private val _state = mutableStateOf(value)
    override val state: State<Boolean> = _state

    override var value: Boolean
        get() = settingsImpl.getBoolean(key, initialValue)
        set(value) {
            settingsImpl[key] = value
            _state.value = value
        }
}

class IntSetting(
    override val key: String,
    override val initialValue: Int
): Setting<Int> {
    private val _state = mutableIntStateOf(value)
    override val state: IntState = _state

    override var value: Int
        get() = settingsImpl.getInt(key, initialValue)
        set(value) {
            settingsImpl[key] = value
            _state.intValue = value
        }
}