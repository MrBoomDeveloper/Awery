package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.PreferenceScreen
import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
interface ConfigurableAnimeSource {
	@ExtensionSdk
	fun setupPreferenceScreen(screen: PreferenceScreen)
}