package com.mrboomdev.awery.extension.loaders

import android.content.SharedPreferences
import com.mrboomdev.awery.extension.sdk.Preferences

class AndroidPreferences(
	private val delegate: SharedPreferences,
	private val commit: Boolean = true
): Preferences {
	override fun putStringSet(key: String, value: Set<String>) {
		delegate.edit().putStringSet(key, value).save()
	}

	override fun getStringSet(key: String): Set<String> {
		return delegate.getStringSet(key, null) ?: emptySet()
	}

	override fun getString(key: String): String? {
		return delegate.getString(key, null)
	}

	override fun putString(key: String, value: String?) {
		delegate.edit().putString(key, value).save()
	}

	override fun getInt(key: String): Int? {
		if(!delegate.contains(key)) {
			return null
		}
		
		return delegate.getInt(key, 0)
	}

	override fun putInt(key: String, value: Int?) {
		if(value == null) return remove(key)
		delegate.edit().putInt(key, value).save()
	}

	override fun getBoolean(key: String): Boolean? {
		if(!delegate.contains(key)) {
			return null
		}

		return delegate.getBoolean(key, false)
	}

	override fun putBoolean(key: String, value: Boolean?) {
		if(value == null) return remove(key)
		delegate.edit().putBoolean(key, value).save()
	}

	override fun remove(key: String) {
		delegate.edit().remove(key).save()
	}
	
	private fun SharedPreferences.Editor.save() {
		if(commit) commit() else apply()
	}
}