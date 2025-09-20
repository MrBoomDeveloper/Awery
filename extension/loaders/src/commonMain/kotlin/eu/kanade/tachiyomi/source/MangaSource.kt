package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.core.utils.PlatformSdk
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.util.awaitSingle
import rx.Observable

/**
 * A basic interface for creating a source. It could be an online source, a local source, etc.
 */
@PlatformSdk
interface MangaSource {

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

    @PlatformSdk
    val lang: String
        get() = ""

    /**
     * Get the updated details for a manga.
     *
     * @since extensions-lib 1.5
     * @param manga the manga to update.
     * @return the updated manga.
     */
    @PlatformSdk
    suspend fun getMangaDetails(manga: SManga): SManga {
        @Suppress("DEPRECATION")
        return fetchMangaDetails(manga).awaitSingle()
    }

    /**
     * Get all the available chapters for a manga.
     *
     * @since extensions-lib 1.5
     * @param manga the manga to update.
     * @return the chapters for the manga.
     */
    @PlatformSdk
    suspend fun getChapterList(manga: SManga): List<SChapter> {
        @Suppress("DEPRECATION")
        return fetchChapterList(manga).awaitSingle()
    }

    /**
     * Get the list of pages a chapter has. Pages should be returned
     * in the expected order; the index is ignored.
     *
     * @since extensions-lib 1.5
     * @param chapter the chapter.
     * @return the pages for the chapter.
     */
    @PlatformSdk
    suspend fun getPageList(chapter: SChapter): List<Page> {
        @Suppress("DEPRECATION")
        return fetchPageList(chapter).awaitSingle()
    }

    @PlatformSdk
    @Deprecated("Deprecated in Tachiyomi")
    fun fetchMangaDetails(manga: SManga): Observable<SManga> =
        throw IllegalStateException("Not used")

    @PlatformSdk
    @Deprecated("Deprecated in Tachiyomi")
    fun fetchChapterList(manga: SManga): Observable<List<SChapter>> =
        throw IllegalStateException("Not used")

    @PlatformSdk
    @Deprecated("Deprecated in Tachiyomi")
    fun fetchPageList(chapter: SChapter): Observable<List<Page>> =
        throw IllegalStateException("Not used")
}