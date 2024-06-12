package com.mrboomdev.awery.extensions.support.yomi.aniyomi

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import eu.kanade.tachiyomi.util.lang.awaitSingle
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Just a little bridge to use all Coroutine based methods.
 * @author MrBoomDev
 */
object AniyomiKotlinBridge {

    interface ResponseCallback<T> {
        fun onResponse(data: T?, e: Throwable?)
    }

    @JvmStatic
    fun searchAnime(
        source: AnimeCatalogueSource,
        page: Int,
        query: String,
        filters: AnimeFilterList,
        callback: ResponseCallback<AnimesPage>
    ) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }
        val scope = CoroutineScope(Job())

        scope.launch(exceptionHandler) {
            callback.onResponse(source.getSearchAnime(page + 1, query, filters), null)
        }
    }

    @JvmStatic
    fun getPopularAnime(source: AnimeCatalogueSource, page: Int, callback: ResponseCallback<AnimesPage>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }
        val scope = CoroutineScope(Job())

        scope.launch(exceptionHandler) {
            callback.onResponse(source.getPopularAnime(page + 1), null)
        }
    }

    @JvmStatic
    fun getLatestAnime(source: AnimeCatalogueSource, page: Int, callback: ResponseCallback<AnimesPage>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }
        val scope = CoroutineScope(Job())

        scope.launch(exceptionHandler) {
            callback.onResponse(source.getLatestUpdates(page + 1), null)
        }
    }

    @JvmStatic
    fun getAnimeDetails(source: AnimeSource, anime: SAnime, callback: ResponseCallback<SAnime>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }
        val scope = CoroutineScope(Job())

        scope.launch(exceptionHandler) {
            callback.onResponse(source.getAnimeDetails(anime), null)
        }
    }

    @JvmStatic
    fun getEpisodesList(source: AnimeSource, anime: SAnime, callback: ResponseCallback<List<SEpisode>>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }
        val scope = CoroutineScope(Job())

        scope.launch(exceptionHandler) {
            callback.onResponse(source.getEpisodeList(anime), null)
        }
    }

    @JvmStatic
    fun getVideosList(source: AnimeSource, episode: SEpisode, callback: ResponseCallback<List<Video>>) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }
        val scope = CoroutineScope(Job())

        scope.launch(exceptionHandler) {
            callback.onResponse(source.getVideoList(episode), null)
        }
    }
}