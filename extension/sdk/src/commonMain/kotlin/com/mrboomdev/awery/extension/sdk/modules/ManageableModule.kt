package com.mrboomdev.awery.extension.sdk.modules

import com.mrboomdev.awery.extension.sdk.Preference

interface ManageableModule: Module {
	/**
	 * Example:
	 * ```kotlin
	 *  var username = ""
	 * 
	 *  override fun getPreferences() = listOf(
	 *  	StringPreference(
	 *  		key = "username",
	 *  		name = "Username",
	 *  		value = username	
	 *  	)
	 *  )
	 * ```
	 * @see onSavePreferences
	 * @return A list of preferences to be used by this extension
	 */
	fun getPreferences(): List<Preference<*>>

	/**
	 * Called to restore saved preferences from the client so
	 * that you can see what user has configured and how.
	 * Example:
	 * ```kotlin
	 *  var username = ""
	 *  
	 *  override fun onSavePreferences(preferences: List<Preference<*>>) {
	 *  	this.username = preferences.first { it.key == "username" }.value as String
	 *  }
	 * ```
	 * @see getPreferences
	 */
	fun onSavePreferences(preferences: List<Preference<*>>)
	
	suspend fun uninstall()
}