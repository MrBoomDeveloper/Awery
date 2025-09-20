package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.core.utils.PlatformSdk
import eu.kanade.tachiyomi.source.model.FilterList
import eu.kanade.tachiyomi.source.model.MangasPage
import rx.Observable
import tachiyomi.core.common.util.lang.awaitSingle

@PlatformSdk
interface CatalogueSource : MangaSource {

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
     * Get a page with a list of manga.
     *
     * @since extensions-lib 1.5
     * @param page the page number to retrieve.
     */
    @PlatformSdk
    suspend fun getPopularManga(page: Int): MangasPage {
        @Suppress("DEPRECATION")
        return fetchPopularManga(page).awaitSingle()
    }

    /**
     * Get a page with a list of manga.
     *
     * @since extensions-lib 1.5
     * @param page the page number to retrieve.
     * @param query the search query.
     * @param filters the list of filters to apply.
     */
    @PlatformSdk
    suspend fun getSearchManga(page: Int, query: String, filters: FilterList): MangasPage {
        @Suppress("DEPRECATION")
        return fetchSearchManga(page, query, filters).awaitSingle()
    }

    /**
     * Get a page with a list of latest manga updates.
     *
     * @since extensions-lib 1.5
     * @param page the page number to retrieve.
     */
    @PlatformSdk
    suspend fun getLatestUpdates(page: Int): MangasPage {
        @Suppress("DEPRECATION")
        return fetchLatestUpdates(page).awaitSingle()
    }

    /**
     * Returns the list of filters for the source.
     */
    @PlatformSdk
    fun getFilterList(): FilterList

    @PlatformSdk
    @Deprecated("Deprecated in Tachiyomi")
    fun fetchPopularManga(page: Int): Observable<MangasPage> =
        throw IllegalStateException("Not used")

    @PlatformSdk
    @Deprecated("Deprecated in Tachiyomi")
    fun fetchSearchManga(page: Int, query: String, filters: FilterList): Observable<MangasPage> =
        throw IllegalStateException("Not used")

    @PlatformSdk
    @Deprecated("Deprecated in Tachiyomi")
    fun fetchLatestUpdates(page: Int): Observable<MangasPage> =
        throw IllegalStateException("Not used")
}