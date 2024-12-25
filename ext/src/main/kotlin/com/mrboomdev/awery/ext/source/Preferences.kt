package com.mrboomdev.awery.ext.source

interface Preferences {
	fun getString(key: String): String?
	fun getInt(key: String): Int?
	fun getBoolean(key: String): Boolean?
	fun getFloat(key: String): Float?

	operator fun set(key: String, value: String?)
	operator fun set(key: String, value: Int?)
	operator fun set(key: String, value: Boolean?)
	operator fun set(key: String, value: Float?)

	fun remove(key: String)
	fun save()
}