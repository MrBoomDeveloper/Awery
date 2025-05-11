package eu.kanade.tachiyomi.source.model

import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
data class MangasPage(val mangas: List<SManga>, val hasNextPage: Boolean)
