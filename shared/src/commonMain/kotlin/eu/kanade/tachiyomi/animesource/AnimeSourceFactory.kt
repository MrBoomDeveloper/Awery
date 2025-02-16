package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
interface AnimeSourceFactory {
	@ExtensionSdk
	fun createSources(): List<AnimeSource>
}