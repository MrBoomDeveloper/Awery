package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.core.utils.PlatformSdk

/**
 * A factory for creating sources at runtime.
 */
@PlatformSdk
interface AnimeSourceFactory {
    /**
     * Create a new copy of the sources
     * @return The created sources
     */
    @PlatformSdk
    fun createSources(): List<AnimeSource>
}