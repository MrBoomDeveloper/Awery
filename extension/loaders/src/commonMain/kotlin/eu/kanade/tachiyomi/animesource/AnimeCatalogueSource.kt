package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.PlatformSdk
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.source.model.FilterList
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import rx.Observable
import tachiyomi.core.common.util.lang.awaitSingle

@PlatformSdk
interface AnimeCatalogueSource : AnimeSource {

    /**
     * An ISO 639-1 compliant language code (two letters in lower case).
     */
    @PlatformSdk
    override val lang: String

    /**
     * Whether the source has support for latest updates.
     */
    @PlatformSdk
    val supportsLatest: Boolean

    /**
     * Get a page with a list of anime.
     *
     * @since extensions-lib 1.5
     * @param page the page number to retrieve.
     */
    @PlatformSdk
    suspend fun getPopularAnime(page: Int): AnimesPage {
        @Suppress("DEPRECATION")
        return fetchPopularAnime(page).awaitSingle()
    }

    /**
     * Get a page with a list of anime.
     *
     * @since extensions-lib 1.5
     * @param page the page number to retrieve.
     * @param query the search query.
     * @param filters the list of filters to apply.
     */
    @PlatformSdk
    suspend fun getSearchAnime(
        page: Int, 
        query: String, 
        filters: AnimeFilterList
    ): AnimesPage {
        @Suppress("DEPRECATION")
        return fetchSearchAnime(page, query, filters).awaitSingle()
    }

    /**
     * Get a page with a list of latest anime updates.
     *
     * @since extensions-lib 1.5
     * @param page the page number to retrieve.
     */
    @PlatformSdk
    suspend fun getLatestUpdates(page: Int): AnimesPage {
        @Suppress("DEPRECATION")
        return fetchLatestUpdates(page).awaitSingle()
    }

    /**
     * Returns the list of filters for the source.
     */
    @PlatformSdk
    fun getFilterList(): AnimeFilterList

    // Should be replaced as soon as Anime Extension reach 1.5
    @Deprecated(
        "Use the non-RxJava API instead",
        ReplaceWith("getPopularAnime"),
    )
    @PlatformSdk
    fun fetchPopularAnime(page: Int): Observable<AnimesPage>

    // Should be replaced as soon as Anime Extension reach 1.5
    @Deprecated(
        "Use the non-RxJava API instead",
        ReplaceWith("getSearchAnime"),
    )
    @PlatformSdk
    fun fetchSearchAnime(
        page: Int, 
        query: String, 
        filters: AnimeFilterList
    ): Observable<AnimesPage>

    // Should be replaced as soon as Anime Extension reach 1.5
    @Deprecated(
        "Use the non-RxJava API instead",
        ReplaceWith("getLatestUpdates"),
    )
    @PlatformSdk
    fun fetchLatestUpdates(page: Int): Observable<AnimesPage>

    /**
     * Whether parsing related animes in anime page or 
     * extension provide custom related animes request.
     * @default false
     * @since komikku/extensions-lib 1.6
     */
    @PlatformSdk
    val supportsRelatedAnimes: Boolean get() = false

    /**
     * Extensions doesn't want to use App's [getRelatedAnimeListBySearch].
     * @default false
     * @since komikku/extensions-lib 1.6
     */
    @PlatformSdk
    val disableRelatedAnimesBySearch: Boolean get() = false

    /**
     * Disable showing any related animes.
     * @default false
     * @since komikku/extensions-lib 1.6
     */
    @PlatformSdk
    val disableRelatedAnimes: Boolean get() = false

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
    override suspend fun getRelatedAnimeList(
        anime: SAnime,
        exceptionHandler: (Throwable) -> Unit,
        pushResults: suspend (relatedAnime: Pair<String, List<SAnime>>, completed: Boolean) -> Unit,
    ) {
        val handler = CoroutineExceptionHandler { _, e -> exceptionHandler(e) }
        if (!disableRelatedAnimes) {
            supervisorScope {
                if(supportsRelatedAnimes) {
                    launch(handler) {
                        getRelatedAnimeListByExtension(anime, pushResults)
                    }
                }
                
                if(!disableRelatedAnimesBySearch) {
                    launch(handler) {
                        getRelatedAnimeListBySearch(anime, pushResults)
                    }
                }
            }
        }
    }

    /**
     * Get related animes provided by extension
     *
     * @return a list of <keyword, related animes>
     * @since komikku/extensions-lib 1.6
     */
    @PlatformSdk
    suspend fun getRelatedAnimeListByExtension(
        anime: SAnime,
        pushResults: suspend (relatedAnime: Pair<String, List<SAnime>>, completed: Boolean) -> Unit,
    ) {
        runCatching { fetchRelatedAnimeList(anime) }
            .onSuccess { if (it.isNotEmpty()) pushResults(Pair("", it), false) }
            .onFailure { e ->
                Log.e("AnimeCatalogueSource", "getRelatedAnimeListByExtension", e)
            }
    }

    /**
     * Fetch related animes for a anime from source/site.
     *
     * @since komikku/extensions-lib 1.6
     * @param anime the current anime to get related animes.
     * @return the related animes for the current anime.
     * @throws UnsupportedOperationException if a source doesn't support related animes.
     */
    @PlatformSdk
    suspend fun fetchRelatedAnimeList(anime: SAnime): List<SAnime> = 
        throw UnsupportedOperationException("Unsupported!")

    /**
     * Slit & strip anime's title into separate searchable keywords.
     * Used for searching related animes.
     *
     * @since komikku/extensions-lib 1.6
     * @return List of keywords.
     */
    @PlatformSdk
    fun String.stripKeywordForRelatedAnimes(): List<String> {
        val regexWhitespace = Regex("\\s+")
        val regexSpecialCharacters =
            Regex("([!~#$%^&*+_|/\\\\,?:;'“”‘’\"<>(){}\\[\\]。・～：—！？、―«»《》〘〙【】「」｜]|\\s-|-\\s|\\s\\.|\\.\\s)")
        val regexNumberOnly = Regex("^\\d+$")

        return replace(regexSpecialCharacters, " ")
            .split(regexWhitespace)
            .map {
                // remove number only
                it.replace(regexNumberOnly, "")
                    .lowercase()
            }
            // exclude single character
            .filter { it.length > 1 }
    }

    /**
     * Get related animes by searching for each keywords from anime's title.
     *
     * @return a list of <keyword, related animes>
     * @since komikku/extensions-lib 1.6
     */
    @PlatformSdk
    suspend fun getRelatedAnimeListBySearch(
        anime: SAnime,
        pushResults: suspend (relatedAnime: Pair<String, List<SAnime>>, completed: Boolean) -> Unit,
    ) {
        val words = HashSet<String>()
        words.add(anime.title)
        if (anime.title.lowercase() != anime.title.lowercase()) words.add(anime.title)
        anime.title.stripKeywordForRelatedAnimes()
            .filterNot { word -> words.any { it.lowercase() == word } }
            .onEach { words.add(it) }
        anime.title.stripKeywordForRelatedAnimes()
            .filterNot { word -> words.any { it.lowercase() == word } }
            .onEach { words.add(it) }
        if (words.isEmpty()) return

        coroutineScope {
            words.map { keyword ->
                launch {
                    runCatching {
                        getSearchAnime(1, keyword, AnimeFilterList()).animes
                    }
                        .onSuccess { if (it.isNotEmpty()) pushResults(Pair(keyword, it), false) }
                        .onFailure { e ->
                            Log.e("AnimeCatalogueSource", "getRelatedAnimeListBySearch", e)
                        }
                }
            }
        }
    }
}