package com.mrboomdev.awery.extensions.support.yomi.aniyomi

import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.async.AsyncUtils
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilterList
import eu.kanade.tachiyomi.animesource.model.AnimesPage
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.model.Video
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Just a little bridge to use all Coroutine based methods.
 * @author MrBoomDev
 */
object AniyomiKotlinBridge {

    @JvmStatic
    fun searchAnime(
        source: AnimeCatalogueSource,
        page: Int,
        query: String,
        filters: AnimeFilterList
    ): AsyncFuture<AnimesPage> {
        return AsyncUtils.controllableFuture {
            CoroutineScope(Job()).launch(CoroutineExceptionHandler { _, e -> it.fail(e) }) {
                it.complete(source.getSearchAnime(page + 1, query, filters))
            }
        }
    }

    @JvmStatic
    fun getPopularAnime(source: AnimeCatalogueSource, page: Int): AsyncFuture<AnimesPage> {
        return AsyncUtils.controllableFuture {
            CoroutineScope(Job()).launch(CoroutineExceptionHandler { _, e -> it.fail(e) }) {
                it.complete(source.getPopularAnime(page + 1))
            }
        }
    }

    @JvmStatic
    fun getLatestAnime(source: AnimeCatalogueSource, page: Int): AsyncFuture<AnimesPage> {
        return AsyncUtils.controllableFuture {
            CoroutineScope(Job()).launch(CoroutineExceptionHandler { _, e -> it.fail(e) }) {
                it.complete(source.getLatestUpdates(page + 1))
            }
        }
    }

    @JvmStatic
    fun getAnimeDetails(source: AnimeSource, anime: SAnime): AsyncFuture<SAnime> {
        return AsyncUtils.controllableFuture {
            CoroutineScope(Job()).launch(CoroutineExceptionHandler { _, e -> it.fail(e) }) {
                it.complete(source.getAnimeDetails(anime))
            }
        }
    }

    @JvmStatic
    fun getEpisodesList(source: AnimeSource, anime: SAnime): AsyncFuture<List<SEpisode>> {
        return AsyncUtils.controllableFuture {
            CoroutineScope(Job()).launch(CoroutineExceptionHandler { _, e -> it.fail(e) }) {
                it.complete(source.getEpisodeList(anime))
            }
        }
    }

    @JvmStatic
    fun getVideosList(source: AnimeSource, episode: SEpisode): AsyncFuture<List<Video>> {
        return AsyncUtils.controllableFuture {
            CoroutineScope(Job()).launch(CoroutineExceptionHandler { _, e -> it.fail(e) }) {
                it.complete(source.getVideoList(episode))
            }
        }
    }
}