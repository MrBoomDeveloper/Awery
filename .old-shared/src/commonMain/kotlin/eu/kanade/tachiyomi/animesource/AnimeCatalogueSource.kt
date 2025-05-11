package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.utils.ExtensionSdk
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.util.awaitSingle
import rx.Observable

@ExtensionSdk
interface AnimeCatalogueSource: AnimeSource {
	val lang: String
	val supportsLatest: Boolean
	fun getFilterList(): AnimeFilterList
	
	/**
	 * @since extension-lib 14
	 */
	@Suppress("DEPRECATION")
	suspend fun getPopularAnime(page: Int): AnimesPage = 
		fetchPopularAnime(page).awaitSingle()
	
	/**
	 * @since extension-lib 14
	 */
	@Suppress("DEPRECATION")
	suspend fun getSearchAnime(page: Int, query: String, filters: AnimeFilterList): AnimesPage =
		fetchSearchAnime(page, query, filters).awaitSingle()
	
	/**
	 * @since extension-lib 14
	 */
	@Suppress("DEPRECATION")
	suspend fun getLatestUpdates(page: Int): AnimesPage =
		fetchLatestUpdates(page).awaitSingle()
	
	@Deprecated("Use the non-RxJava API instead")
	fun fetchPopularAnime(page: Int): Observable<AnimesPage>
	
	@Deprecated("Use the non-RxJava API instead")
	fun fetchSearchAnime(page: Int, query: String, filters: AnimeFilterList): Observable<AnimesPage>
	
	@Deprecated("Use the non-RxJava API instead")
	fun fetchLatestUpdates(page: Int): Observable<AnimesPage>
}