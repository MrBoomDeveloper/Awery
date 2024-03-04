package com.mrboomdev.awery.catalog.extensions.support.aniyomi

import com.mrboomdev.awery.AweryApp
import com.mrboomdev.awery.catalog.template.CatalogVideo
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.runBlocking

object AniyomiKotlinBridge {

    interface VideosListResponse {
        fun onResponse(videos: List<Video>?, e: Throwable?)
    }

    interface EpisodesListResponse {
        fun onResponse(videos: List<SEpisode>?, e: Throwable?)
    }

    @JvmStatic
    fun getEpisodesList(source: AnimeCatalogueSource, anime: SAnime, callback: EpisodesListResponse) {
        runBlocking {
            try {
                callback.onResponse(source.getEpisodeList(anime), null)
            } catch(e: Throwable) {
                callback.onResponse(null, e)
            }
        }
    }

    @JvmStatic
    fun getVideosList(source: AnimeCatalogueSource, episode: SEpisode, callback: VideosListResponse) {
        runBlocking {
            try {
                callback.onResponse(source.getVideoList(episode), null)
            } catch(e: Throwable) {
                callback.onResponse(null, e)
            }
        }
    }
}