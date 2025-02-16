package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
enum class AnimeUpdateStrategy {
    ALWAYS_UPDATE,
    @Suppress("unused")
    ONLY_FETCH_ONCE
}