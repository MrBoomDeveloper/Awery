package com.mrboomdev.awery.extension.sdk

interface Preferences {
	fun putStringSet(key: String, value: Set<String>)
	fun getStringSet(key: String): Set<String>
	
	fun getString(key: String): String?
	fun putString(key: String, value: String?)
	
	fun getInt(key: String): Int?
	fun putInt(key: String, value: Int?)
	
	fun getBoolean(key: String): Boolean?
	fun putBoolean(key: String, value: Boolean?)
	
	fun remove(key: String)
}