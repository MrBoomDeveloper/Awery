package com.mrboomdev.awery.data.settings

import androidx.compose.runtime.FloatState
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LongState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.mrboomdev.awery.core.utils.toEnumOrNull
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.set

@PublishedApi
internal val settings by lazy { createSettings() }
internal expect fun createSettings(): ObservableSettings

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

    override var value
        get() = settings.getString(key, initialValue.name).toEnumOrNull(initialValue::class) ?: initialValue
        set(value) {
            settings[key] = value.name
            _state.value = value
        }
}

class StringSetting(
    override val key: String,
    override val initialValue: String
): Setting<String> {
    private val _state = mutableStateOf(value)
    override val state: State<String> = _state

    override var value
        get() = settings.getString(key, initialValue)
        set(value) {
            settings[key] = value
            _state.value = value
        }
}

class BooleanSetting(
    override val key: String,
    override val initialValue: Boolean
): Setting<Boolean> {
    private val _state = mutableStateOf(value)
    override val state: State<Boolean> = _state

    override var value
        get() = settings.getBoolean(key, initialValue)
        set(value) {
            settings[key] = value
            _state.value = value
        }
    
    fun toggle() {
        value = !value
    }
}

class IntSetting(
    override val key: String,
    override val initialValue: Int
): Setting<Int> {
    private val _state = mutableIntStateOf(value)
    override val state: IntState = _state

    override var value
        get() = settings.getInt(key, initialValue)
        set(value) {
            settings[key] = value
            _state.intValue = value
        }
}

class LongSetting(
    override val key: String,
    override val initialValue: Long
): Setting<Long> {
    private val _state = mutableLongStateOf(value)
    override val state: LongState = _state

    override var value
        get() = settings.getLong(key, initialValue)
        set(value) {
            settings[key] = value
            _state.longValue = value
        }
}