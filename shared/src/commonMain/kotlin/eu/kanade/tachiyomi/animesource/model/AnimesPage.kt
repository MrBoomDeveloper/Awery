package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
data class AnimesPage(val animes: List<SAnime>, val hasNextPage: Boolean)
