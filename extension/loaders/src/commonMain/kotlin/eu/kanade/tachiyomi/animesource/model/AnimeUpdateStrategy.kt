package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
enum class AnimeUpdateStrategy {
    ALWAYS_UPDATE,
    ONLY_FETCH_ONCE
}