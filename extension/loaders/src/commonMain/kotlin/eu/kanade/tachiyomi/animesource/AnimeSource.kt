package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.core.utils.PlatformSdk
import eu.kanade.tachiyomi.animesource.model.Hoster
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.util.awaitSingle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import rx.Observable

/**
 * A basic interface for creating a source. It could be an online source, a local source, etc.
 */
@PlatformSdk
interface AnimeSource {

    /**
     * ID for the source. Must be unique.
     */
    @PlatformSdk
    val id: Long

    /**
     * Name of the source.
     */
    @PlatformSdk
    val name: String

    val lang: String
        get() = ""

    /**
     * Get the updated details for a anime.
     *
     * @since extensions-lib 1.5
     * @param anime the anime to update.
     * @return the updated anime.
     */
    @Suppress("DEPRECATION")
    @PlatformSdk
    suspend fun getAnimeDetails(anime: SAnime): SAnime {
        return fetchAnimeDetails(anime).awaitSingle()
    }

    /**
     * Get all the available episodes for a anime.
     *
     * @since extensions-lib 1.5
     * @param anime the anime to update.
     * @return the episodes for the anime.
     */
    @Suppress("DEPRECATION")
    @PlatformSdk
    suspend fun getEpisodeList(anime: SAnime): List<SEpisode> {
        return fetchEpisodeList(anime).awaitSingle()
    }

    /**
     * Get the list of hoster for an episode. The first hoster in the list should
     * be the preferred hoster.
     *
     * @since extensions-lib 16
     * @param episode the episode.
     * @return the hosters for the episode.
     */
    suspend fun getHosterList(episode: SEpisode): List<Hoster> = throw IllegalStateException("Not used")

    /**
     * Get the list of videos for a hoster.
     *
     * @since extensions-lib 16
     * @param hoster the hoster.
     * @return the videos for the hoster.
     */
    suspend fun getVideoList(hoster: Hoster): List<Video> = throw IllegalStateException("Not used")

    /**
     * Get the list of videos a episode has. Pages should be returned
     * in the expected order; the index is ignored.
     *
     * @since extensions-lib 1.5
     * @param episode the episode.
     * @return the videos for the episode.
     */
    @Suppress("DEPRECATION")
    @PlatformSdk
    suspend fun getVideoList(episode: SEpisode): List<Video> {
        return fetchVideoList(episode).awaitSingle()
    }

    @Deprecated(
        "Use the non-RxJava API instead",
        ReplaceWith("getAnimeDetails"),
    )
    @PlatformSdk
    fun fetchAnimeDetails(anime: SAnime): Observable<SAnime> =
        throw IllegalStateException("Not used")

    @Deprecated(
        "Use the non-RxJava API instead",
        ReplaceWith("getEpisodeList"),
    )
    @PlatformSdk
    fun fetchEpisodeList(anime: SAnime): Observable<List<SEpisode>> =
        throw IllegalStateException("Not used")

    @Deprecated(
        "Use the non-RxJava API instead",
        ReplaceWith("getVideoList"),
    )
    @PlatformSdk
    fun fetchVideoList(episode: SEpisode): Observable<List<Video>> =
        throw IllegalStateException("Not used")

    /**
     * Get all the available related animes for a anime.
     * Normally it's not needed to override this method.
     *
     * @since komikku/extensions-lib 1.6
     * @param anime the current anime to get related animes.
     * @return a list of <keyword, related animes>
     * @throws UnsupportedOperationException if a source doesn't support related animes.
     */
    @PlatformSdk
    suspend fun getRelatedAnimeList(
        anime: SAnime,
        exceptionHandler: (Throwable) -> Unit,
        pushResults: suspend (relatedAnime: Pair<String, List<SAnime>>, completed: Boolean) -> Unit,
    ): Unit = throw UnsupportedOperationException()
}