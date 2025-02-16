package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.utils.ExtensionSdk
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.util.awaitSingle
import rx.Observable

@ExtensionSdk
interface AnimeSource {
	val id: Long
	val name: String
	
	/**
	 * @since extension-lib 14
	 */
	@Suppress("DEPRECATION")
	suspend fun getAnimeDetails(anime: SAnime): SAnime = 
		fetchAnimeDetails(anime).awaitSingle()
	
	@Suppress("DEPRECATION")
	suspend fun getEpisodeList(anime: SAnime): List<SEpisode> =
		fetchEpisodeList(anime).awaitSingle()
	
	@Suppress("DEPRECATION")
	suspend fun getVideoList(episode: SEpisode): List<Video> =
		fetchVideoList(episode).awaitSingle()
	
	@Deprecated("Use the 1.x API instead")
	fun fetchAnimeDetails(anime: SAnime): Observable<SAnime> = throw IllegalStateException("Not used")
	
	@Deprecated("Use the 1.x API instead")
	fun fetchEpisodeList(anime: SAnime): Observable<List<SEpisode>> = throw IllegalStateException("Not used")
	
	@Deprecated("Use the 1.x API instead")
	fun fetchVideoList(episode: SEpisode): Observable<List<Video>> = Observable.empty()
}