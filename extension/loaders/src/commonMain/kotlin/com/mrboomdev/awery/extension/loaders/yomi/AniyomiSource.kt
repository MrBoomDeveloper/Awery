package com.mrboomdev.awery.extension.loaders.yomi

import com.mrboomdev.awery.core.utils.deserialize
import com.mrboomdev.awery.core.utils.serialize
import com.mrboomdev.awery.extension.sdk.BooleanPreference
import com.mrboomdev.awery.extension.sdk.Either
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.sdk.Feed
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.Image
import com.mrboomdev.awery.extension.sdk.LabelPreference
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.PreferenceGroup
import com.mrboomdev.awery.extension.sdk.Results
import com.mrboomdev.awery.extension.sdk.SelectPreference
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.TriStatePreference
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.extension.sdk.WatchVariant
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import eu.kanade.tachiyomi.animesource.AnimeCatalogueSource
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.model.AnimeFilter
import eu.kanade.tachiyomi.animesource.model.SEpisode
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AniyomiSource(
    private val extension: Extension,
    val source: AnimeSource,
    override val isNsfw: Boolean
): Extension {
    override val name: String
        get() = source.name
    
    override val id: String
        get() = "${extension.id}_${source.id}"

    override val version: String
        get() = extension.version

    override val lang: String?
        get() = source.lang.takeIf { it != "all" }

    override val webpage: String?
        get() = if(source is AnimeHttpSource) {
            source.baseUrl
        } else null
    
    override val icon: Image?
        get() = extension.icon

    override val loadException: ExtensionLoadException?
        get() = null

    @OptIn(ExperimentalEncodingApi::class)
    override fun createModules() = buildList {
        if(source is AnimeCatalogueSource) {
            add(object : CatalogModule {
                override suspend fun getDefaultFilters(): List<Preference<*>> {
                    return source.getFilterList().list.toAweryFilters() + 
                            StringPreference(
                                key = "ANIYOMI_QUERY", 
                                name = "Query", 
                                value = "",
                                role = Preference.Role.QUERY
                            )
                }

                override suspend fun search(
                    filters: List<Preference<*>>,
                    page: Int
                ): Results<Media> {
                    return source.getSearchAnime(
                        page = page,
                        query = filters.first { it.role == Preference.Role.QUERY }.value.toString(),
                        filters = source.getFilterList().apply { 
                            applyAweryFilters(filters)
                        }
                    ).let { page ->
                        Results(page.animes.distinctBy { it.url }.map { anime -> 
                            anime.toMedia(source, isNsfw) 
                        }, page.hasNextPage)
                    }
                }

                override suspend fun updateMedia(media: Media): Media {
                    return media.toSAnime().let { anime ->
                        source.getAnimeDetails(anime).toMedia(source, isNsfw, anime)
                    }
                }

                override suspend fun getFeeds(page: Int): Results<Feed> {
                    return Results(buildList {
                        if(source.supportsLatest) {
                            add(Feed("latest", "Latest"))
                        }

                        add(Feed("popular", "Popular"))
                    }, false)
                }

                override suspend fun loadFeed(feed: Feed, page: Int): Results<Media> {
                    return when(feed.id) {
                        "latest" -> source.getLatestUpdates(page)
                        "popular" -> source.getPopularAnime(page)
                        else -> throw IllegalArgumentException("Unknown feed: ${feed.id}!")
                    }.let { page ->
                        Results(page.animes.distinctBy { it.url }.map { anime ->
                            anime.toMedia(source, isNsfw) 
                        }, page.hasNextPage)
                    }
                }
            })
        }

        add(object : WatchModule {
            val TYPE_EPISODE = "__EPISODE__"
            val TYPE_VIDEO = "__VIDEO__"

            override suspend fun watch(media: Media, page: Int): Either<Video, Results<WatchVariant>> {
                return source.getEpisodeList(media.toSAnime()).map { episode ->
                    WatchVariant(
                        id = TYPE_EPISODE + Base64.encode(episode.serialize()),
                        releaseDate = episode.date_upload,
                        number = episode.episode_number,
                        title = episode.name,
                        type = WatchVariant.Type.EPISODE
                    )
                }.let { Either.second(Results(it, false)) }
            }

            @Suppress("DEPRECATION")
            override suspend fun watch(
                watchVariant: WatchVariant,
                page: Int
            ): Either<Video, Results<WatchVariant>> {
                return when {
                    watchVariant.id.startsWith(TYPE_EPISODE) -> {
                        val encodedVideo = watchVariant.id.substringAfter(TYPE_EPISODE)
                        val episode = Base64.decode(encodedVideo).deserialize() as SEpisode

                        Either.second(Results(source.getVideoList(episode).map { animeVideo ->
                            val aweryVideo = Video(
                                url = animeVideo.videoUrl.takeIf { it.isNotBlank() } ?: animeVideo.url,
                                title = animeVideo.videoTitle.takeIf { it.isNotBlank() },
                                headers = animeVideo.headers?.toMap() ?: emptyMap(),

                                subtitleTracks = animeVideo.subtitleTracks.map { track ->
                                    Video.Track(url = track.url, locale = track.lang)
                                },

                                audioTracks = animeVideo.audioTracks.map { track ->
                                    Video.Track(url = track.url, locale = track.lang)
                                },
                            )
                            
                            WatchVariant(
                                id = TYPE_VIDEO + Json.encodeToString(aweryVideo),
                                title = animeVideo.videoTitle,
                                type = WatchVariant.Type.QUALITY
                            )
                        }, false))
                    }

                    watchVariant.id.startsWith(TYPE_VIDEO) -> {
                        Either.first(Json.decodeFromString(watchVariant.id.substringAfter(TYPE_VIDEO)))
                    }

                    else -> throw IllegalArgumentException("Unknown watch variant type! ${watchVariant.id}")
                }
            }
        })
    }
}

private fun Collection<AnimeFilter<*>>.toAweryFilters(): List<Preference<*>> = mapNotNull { filter ->
	when(filter) {
        is AnimeFilter.CheckBox -> BooleanPreference(
            key = filter.name,
            value = filter.state
        )
        
        is AnimeFilter.Text -> StringPreference(
            key = filter.name,
            value = filter.state
        )
        
        is AnimeFilter.TriState -> TriStatePreference(
            key = filter.name,
            value = when {
                filter.isIncluded() -> TriStatePreference.State.INCLUDED
                filter.isExcluded() -> TriStatePreference.State.EXCLUDED
                else -> TriStatePreference.State.NONE
            }
        )
        
        is AnimeFilter.Select<*> -> SelectPreference(
            key = filter.name,
            value = filter.state.toString(),
            values = filter.values.mapIndexed { index, value -> 
                SelectPreference.Item(
                    key = index.toString(),
                    name = value.toString()
                )
            }
        )
        
        is AnimeFilter.Sort -> SelectPreference(
            key = filter.name,
                
            value = filter.state?.let { selected ->
                buildString {
                    append(selected.index)
                    append(if(selected.ascending) "_asc_" else "_desc_")
                }
            } ?: "0_desc_",
                
            values = filter.values.flatMapIndexed { index, value ->
                listOf(
                    SelectPreference.Item(
                        key = index.toString() + "_desc_",
                        name = "$value descending"
                    ),

                    SelectPreference.Item(
                        key = index.toString() + "_asc_",
                        name = "$value ascending"
                    )
                )
            }
        )
        
        is AnimeFilter.Group<*> -> PreferenceGroup(
            key = filter.name,
            items = filter.state.let {
                @Suppress("UNCHECKED_CAST")
                it as Collection<AnimeFilter<*>>
            }.toAweryFilters()
        )
        
        is AnimeFilter.Header -> LabelPreference(filter.name)
        
        is AnimeFilter.Separator -> null
    }
}

private fun Collection<AnimeFilter<*>>.applyAweryFilters(filters: Collection<Preference<*>>) {
    forEach { filter ->
        when(filter) {
            is AnimeFilter.CheckBox -> {
                filter.state = filters.first {
                    it.key == filter.name
                }.value as Boolean
            }

            is AnimeFilter.Text -> {
                filter.state = filters.first {
                    it.key == filter.name
                }.value as String
            }

            is AnimeFilter.Select<*> -> {
                filter.state = filters.first {
                    it.key == filter.name
                }.let { it.value as String }.toInt()
            }
            
            is AnimeFilter.Sort -> {
                filter.state = filters.first {
                    it.key == filter.name
                }.let { it.value as String }.let { value ->
                    val isAscending = value.endsWith("_asc_")
                    val index = value.substringBefore("_").toInt()
                    AnimeFilter.Sort.Selection(index, isAscending)
                }
            }

            is AnimeFilter.TriState -> {
                filter.state = filters.first {
                    it.key == filter.name
                }.let {
                    when(it.value as TriStatePreference.State) {
                        TriStatePreference.State.INCLUDED -> AnimeFilter.TriState.STATE_INCLUDE
                        TriStatePreference.State.EXCLUDED -> AnimeFilter.TriState.STATE_EXCLUDE
                        TriStatePreference.State.NONE -> AnimeFilter.TriState.STATE_IGNORE
                    }
                }
            }
            
            is AnimeFilter.Group<*> -> {
                val aweryFiltersGroup = filters.first { 
                    it.key == filter.name
                }.let { it as PreferenceGroup }.items
                
                val aniyomiFiltersGroup = filter.state.let {
                    @Suppress("UNCHECKED_CAST")
                    it as Collection<AnimeFilter<*>>
                }
                
                aniyomiFiltersGroup.applyAweryFilters(aweryFiltersGroup)
            }
            
            is AnimeFilter.Header, is AnimeFilter.Separator -> {}
        }
    }
}