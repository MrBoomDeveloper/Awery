package com.mrboomdev.awery.extension.bundled.anilist

import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.Preferences
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.modules.ManageableModule

class AnilistPreferences(private val prefs: Preferences): ManageableModule {
	override fun getPreferences() = buildList { 
		add(StringPreference(
			key = "token",
			name = "Token",
			value = prefs.getString("token") ?: ""
		))
	}

	override fun onSavePreferences(preferences: List<Preference<*>>) {
		prefs.putString("token", preferences.first { it.key == "token" }.value as String)
	}

	override suspend fun uninstall() {
		throw UnsupportedOperationException("Anilist cannot be uninstalled!")
	}
}