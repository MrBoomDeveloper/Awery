package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.utils.ExtensionSdk

/**
 * A factory for creating sources at runtime.
 */
@ExtensionSdk
interface SourceFactory {
    /**
     * Create a new copy of the sources
     * @return The created sources
     */
    @ExtensionSdk
    fun createSources(): List<Source>
}
