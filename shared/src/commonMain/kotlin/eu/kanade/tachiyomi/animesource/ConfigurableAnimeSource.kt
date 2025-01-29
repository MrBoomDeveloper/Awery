package eu.kanade.tachiyomi.animesource

import androidx.preference.PreferenceScreen

interface ConfigurableAnimeSource {
	fun setupPreferenceScreen(screen: PreferenceScreen)
}