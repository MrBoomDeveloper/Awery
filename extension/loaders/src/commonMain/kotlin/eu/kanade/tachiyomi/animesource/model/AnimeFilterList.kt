package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
data class AnimeFilterList(
    val list: List<AnimeFilter<*>>
): List<AnimeFilter<*>> by list {
    @PlatformSdk
    constructor(
        vararg fs: AnimeFilter<*>
    ): this(if(fs.isNotEmpty()) fs.asList() else emptyList())
}