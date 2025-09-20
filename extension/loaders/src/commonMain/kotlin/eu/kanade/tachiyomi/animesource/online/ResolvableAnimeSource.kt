package eu.kanade.tachiyomi.animesource.online

import com.mrboomdev.awery.core.utils.PlatformSdk
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode

/**
 * A source that may handle opening an SAnime or SEpisode for a given URI.
 *
 * @since extensions-lib 1.5
 */
@PlatformSdk
interface ResolvableAnimeSource : AnimeSource {

    /**
     * Returns what the given URI may open.
     * Returns [UriType.Unknown] if the source is not able to resolve the URI.
     *
     * @since extensions-lib 1.5
     */
    @PlatformSdk
    fun getUriType(uri: String): UriType

    /**
     * Called if [getUriType] is [UriType.Anime].
     * Returns the corresponding SManga, if possible.
     *
     * @since extensions-lib 1.5
     */
    @PlatformSdk
    suspend fun getAnime(uri: String): SAnime?

    /**
     * Called if [getUriType] is [UriType.Episode].
     * Returns the corresponding SChapter, if possible.
     *
     * @since extensions-lib 1.5
     */
    @PlatformSdk
    suspend fun getEpisode(uri: String): SEpisode?
}

@PlatformSdk
sealed interface UriType {
    @PlatformSdk
    data object Anime: UriType

    @PlatformSdk
    data object Episode: UriType

    @PlatformSdk
    data object Unknown: UriType
}