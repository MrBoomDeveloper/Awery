package com.mrboomdev.awery.catalog.extensions.support.aniyomi

import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.runBlocking

object AniyomiKotlinBridge {

    interface SearchResponse {
        fun onResponse(videos: AnimesPage?, e: Throwable?)
    }

    interface VideosListResponse {
        fun onResponse(videos: List<Video>?, e: Throwable?)
    }

    interface EpisodesListResponse {
        fun onResponse(videos: List<SEpisode>?, e: Throwable?)
    }

    @JvmStatic
    fun searchAnime(source: AnimeCatalogueSource, page: Int, query: String, filters: AnimeFilterList, callback: SearchResponse) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }

        runBlocking(exceptionHandler) {
            try {
                callback.onResponse(source.getSearchAnime(page, query, filters), null)
            } catch(e: Throwable) {
                callback.onResponse(null, e)
            }
        }
    }

    @JvmStatic
    fun getEpisodesList(source: AnimeCatalogueSource, anime: SAnime, callback: EpisodesListResponse) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }

        runBlocking(exceptionHandler) {
            try {
                callback.onResponse(source.getEpisodeList(anime), null)
            } catch(e: Throwable) {
                callback.onResponse(null, e)
            }
        }
    }

    @JvmStatic
    fun getVideosList(source: AnimeCatalogueSource, episode: SEpisode, callback: VideosListResponse) {
        val exceptionHandler = CoroutineExceptionHandler { _, e -> callback.onResponse(null, e) }

        runBlocking(exceptionHandler) {
            try {
                callback.onResponse(source.getVideoList(episode), null)
            } catch(e: Throwable) {
                callback.onResponse(null, e)
            }
        }
    }
}