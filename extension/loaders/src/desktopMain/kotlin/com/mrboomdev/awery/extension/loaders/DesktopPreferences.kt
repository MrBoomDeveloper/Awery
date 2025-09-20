package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.extension.sdk.Preferences
import com.russhwolf.settings.Settings
import kotlinx.serialization.json.Json

class DesktopPreferences(
	private val delegate: Settings
): Preferences {
	override fun putStringSet(key: String, value: Set<String>) {
		delegate.putString(
			key = key,
			value = Json.encodeToString<Set<String>>(value)
		)
	}

	override fun getStringSet(key: String): Set<String> {
		return delegate.getStringOrNull(key)?.let { 
			Json.decodeFromString<Set<String>>(it)
		} ?: emptySet()
	}

	override fun getString(key: String): String? {
		return delegate.getStringOrNull(key)
	}

	override fun putString(key: String, value: String?) {
		if(value == null) {
			delegate.remove(key)
			return
		}
		
		delegate.putString(key, value)
	}

	override fun getInt(key: String): Int? {
		return delegate.getIntOrNull(key)
	}

	override fun putInt(key: String, value: Int?) {
		if(value == null) {
			delegate.remove(key)
			return
		}

		delegate.putInt(key, value)
	}

	override fun getBoolean(key: String): Boolean? {
		return delegate.getBooleanOrNull(key)
	}

	override fun putBoolean(key: String, value: Boolean?) {
		if(value == null) {
			delegate.remove(key)
			return
		}

		delegate.putBoolean(key, value)
	}

	override fun remove(key: String) {
		delegate.remove(key)
	}
}