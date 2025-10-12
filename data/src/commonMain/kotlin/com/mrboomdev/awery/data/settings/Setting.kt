package com.mrboomdev.awery.data.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.mrboomdev.awery.core.utils.toEnumOrNull
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@PublishedApi
internal val settings by lazy { createSettings() }
internal expect fun createSettings(): ObservableSettings

sealed class Setting<T>(
	val key: String,
	val initialValue: T
) {
	internal val state by lazy {
		MutableStateFlow(value)
	}
	
	val stateFlow get() = state.asStateFlow()
	
	val value: T get() = readValue() ?: initialValue
	
	suspend fun set(value: T) {
		saveValue(value)
		state.emit(value)
	}
	
	protected abstract fun saveValue(value: T)
	protected abstract fun readValue(): T?
}

@Composable
fun <T> Setting<T>.collectAsState() = stateFlow.collectAsState()

class EnumSetting<T: Enum<T>>(key: String, initialValue: T): Setting<T>(key, initialValue) {
	override fun readValue() = settings.getStringOrNull(key)?.toEnumOrNull(initialValue::class)
	override fun saveValue(value: T) = settings.set(key, value.name)
}

class StringSetting(key: String, initialValue: String): Setting<String>(key, initialValue) {
	override fun readValue() = settings.getStringOrNull(key)
	override fun saveValue(value: String) = settings.set(key, value)
}

class BooleanSetting(key: String, initialValue: Boolean): Setting<Boolean>(key, initialValue) {
	override fun readValue() = settings.getBooleanOrNull(key)
	override fun saveValue(value: Boolean) = settings.set(key, value)
    suspend fun toggle() = set(!value)
}

class IntSetting(key: String, initialValue: Int): Setting<Int>(key, initialValue) {
	override fun readValue() = settings.getIntOrNull(key)
	override fun saveValue(value: Int) = settings.set(key, value)
}

class LongSetting(key: String, initialValue: Long): Setting<Long>(key, initialValue) {
	override fun readValue() = settings.getLongOrNull(key)
	override fun saveValue(value: Long) = settings.set(key, value)
}