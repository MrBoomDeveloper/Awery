package com.mrboomdev.awery.platform

import android.content.SharedPreferences
import android.util.Log
import com.mrboomdev.awery.platform.android.AndroidGlobals
import com.mrboomdev.awery.ext.source.Preferences

private const val TAG = "AppPreferences"
private const val SAME_KEY_DIFFERENT_TYPE = "An existing value with same key, but different type was found!"

actual object PlatformPreferences : Preferences {
	private val prefs = AndroidGlobals.applicationContext.getSharedPreferences("Awery", 0)
	private var editor: SharedPreferences.Editor? = null
	
	private fun ensureEditorExistence() = editor ?: prefs.edit().also { editor = it }
	
	override fun getString(key: String): String? {
		if(!prefs.contains(key)) {
			return null
		}
		
		return try {
			prefs.getString(key, null)
		} catch(e: ClassCastException) {
			Log.e(TAG, "$SAME_KEY_DIFFERENT_TYPE $key")
			null
		}
	}
	
	override fun getInt(key: String): Int? {
		if(!prefs.contains(key)) {
			return null
		}
		
		return try {
			prefs.getInt(key, 0)
		} catch(e: ClassCastException) {
			Log.e(TAG, "$SAME_KEY_DIFFERENT_TYPE $key")
			null
		}
	}
	
	override fun getBoolean(key: String): Boolean? {
		if(!prefs.contains(key)) {
			return null
		}
		
		return try {
			prefs.getBoolean(key, false)
		} catch(e: ClassCastException) {
			Log.e(TAG, "$SAME_KEY_DIFFERENT_TYPE $key")
			null
		}
	}
	
	override fun getFloat(key: String): Float? {
		if(!prefs.contains(key)) {
			return null
		}
		
		return try {
			prefs.getFloat(key, 0f)
		} catch(e: ClassCastException) {
			Log.e(TAG, "$SAME_KEY_DIFFERENT_TYPE $key")
			null
		}
	}
	
	override fun set(key: String, value: String?) {
		ensureEditorExistence().apply { value?.also { putString(key, it) } ?: remove(key) }
	}
	
	override fun set(key: String, value: Int?) {
		ensureEditorExistence().apply { value?.also { putInt(key, it) } ?: remove(key) }
	}
	
	override fun set(key: String, value: Boolean?) {
		ensureEditorExistence().apply { value?.also { putBoolean(key, it) } ?: remove(key) }
	}
	
	override fun set(key: String, value: Float?) {
		ensureEditorExistence().apply { value?.also { putFloat(key, it) } ?: remove(key) }
	}
	
	override fun remove(key: String) {
		ensureEditorExistence().remove(key)
	}
	
	override fun save() {
		editor?.commit()
		editor = null
	}
}