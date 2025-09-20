package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.core.utils.PlatformSdk

/**
 * A factory for creating sources at runtime.
 */
@PlatformSdk
interface SourceFactory {
    /**
     * Create a new copy of the sources
     * @return The created sources
     */
    @PlatformSdk
    fun createSources(): List<MangaSource>
}