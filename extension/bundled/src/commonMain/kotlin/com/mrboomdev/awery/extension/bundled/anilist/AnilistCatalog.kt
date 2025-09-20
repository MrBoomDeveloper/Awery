package com.mrboomdev.awery.extension.bundled.anilist

import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistMedia
import com.mrboomdev.awery.extension.bundled.anilist.entity.toMedia
import com.mrboomdev.awery.extension.bundled.anilist.query.MediaQuery
import com.mrboomdev.awery.extension.bundled.anilist.query.SearchQuery
import com.mrboomdev.awery.extension.sdk.Feed
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Results
import com.mrboomdev.awery.extension.sdk.SelectPreference
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.TriStatePreference
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule

private val feeds = listOf<Pair<String, (Int) -> SearchQuery>>(
    "Recently Updated" to {
        SearchQuery(
            page = it + 1,
            isAdult = false, // TODO: Remove this once adult content filtering would be done
            sort = AnilistMedia.Sort.UPDATED_AT_DESC
        )
    },
    
    "Trending" to {
        SearchQuery(
            page = it + 1,
            isAdult = false, // TODO: Remove this once adult content filtering would be done
            sort = AnilistMedia.Sort.TRENDING_DESC
        )
    },

    "Top rated" to {
        SearchQuery(
            page = it + 1,
            isAdult = false, // TODO: Remove this once adult content filtering would be done
            sort = AnilistMedia.Sort.SCORE_DESC
        )
    },

    "Most favourite" to {
        SearchQuery(
            page = it + 1,
            isAdult = false, // TODO: Remove this once adult content filtering would be done
            sort = AnilistMedia.Sort.FAVOURITES_DESC
        )
    },

    "Popular" to {
        SearchQuery(
            page = it + 1,
            isAdult = false, // TODO: Remove this once adult content filtering would be done
            sort = AnilistMedia.Sort.POPULARITY_DESC
        )
    }
)

object AnilistCatalog: CatalogModule {
    override suspend fun getDefaultFilters() = listOf(
        StringPreference(
            key = "search",
            name = "Search query",
            role = Preference.Role.QUERY,
            value = ""
        ),

        SelectPreference(
            key = "type",
            name = "Type",
            value = "Any",
            values = listOf(
                SelectPreference.Item("Any"),
                SelectPreference.Item(AnilistMedia.Type.ANIME.name, "Anime"),
                SelectPreference.Item(AnilistMedia.Type.MANGA.name, "Manga")
            )
        ),

        SelectPreference(
            key = "status",
            name = "Status",
            value = "Any",
            values = listOf(
                SelectPreference.Item("Any"),
                SelectPreference.Item(AnilistMedia.Status.FINISHED.name, "Finished"),
                SelectPreference.Item(AnilistMedia.Status.RELEASING.name, "Releasing"),
                SelectPreference.Item(AnilistMedia.Status.NOT_YET_RELEASED.name, "Not yet released"),
                SelectPreference.Item(AnilistMedia.Status.HIATUS.name, "Hiatus"),
                SelectPreference.Item(AnilistMedia.Status.CANCELLED.name, "Cancelled")
            )
        ),
        
        SelectPreference(
            key = "sort",
            name = "Sort",
            value = AnilistMedia.Sort.POPULARITY_DESC.name,
            values = listOf(
                SelectPreference.Item(AnilistMedia.Sort.POPULARITY_DESC.name, "Popularity"),
                SelectPreference.Item(AnilistMedia.Sort.TRENDING_DESC.name, "Trending"),
                SelectPreference.Item(AnilistMedia.Sort.SCORE_DESC.name, "Score"),
                SelectPreference.Item(AnilistMedia.Sort.FAVOURITES_DESC.name, "Favourites"),
                SelectPreference.Item(AnilistMedia.Sort.UPDATED_AT_DESC.name, "Update date"),
                SelectPreference.Item(AnilistMedia.Sort.START_DATE_DESC.name, "Start date"),
                SelectPreference.Item(AnilistMedia.Sort.SEARCH_MATCH.name, "Search match")
            )
        ),

        TriStatePreference(
            key = "isAdult",
            name = "Adult content",
            value = TriStatePreference.State.NONE
        )
    )

    override suspend fun search(
        filters: List<Preference<*>>,
        page: Int
    ) = AnilistExtension.query(SearchQuery(
        page = page,
        
        search = filters.first { it.key == "search" }.value as String,

        isAdult = filters.first { it.key == "isAdult" }.let {
            when(it.value as TriStatePreference.State) {
                TriStatePreference.State.INCLUDED -> true
                TriStatePreference.State.EXCLUDED -> false
                TriStatePreference.State.NONE -> null
            } 
        },
        
        type = filters.first { it.key == "type" }
            .takeUnless { it.value == "Any" }
            ?.let { AnilistMedia.Type.valueOf(it.value as String) },

        status = filters.first { it.key == "status" }
            .takeUnless { it.value == "Any" }
            ?.let { AnilistMedia.Status.valueOf(it.value as String) },

        sort = filters.first { it.key == "sort" }
            .let { AnilistMedia.Sort.valueOf(it.value as String) }
    )).let { (pageInfo, media) ->
        Results(
            hasNextPage = pageInfo.hasNextPage,
            items = media.map { it.toMedia() }
        )
    }

    override suspend fun updateMedia(media: Media): Media {
        return AnilistExtension.query(MediaQuery(media.id)).toMedia()
    }

    override suspend fun getFeeds(page: Int): Results<Feed> {
        return Results(
            hasNextPage = false,
            items = feeds.mapIndexed { index, (name, _) ->
                Feed(
                    id = index.toString(),
                    name = name
                )
            }
        )
    }

    override suspend fun loadFeed(
        feed: Feed,
        page: Int
    ): Results<Media> = AnilistExtension.query(feeds[feed.id.toInt()].second(page))
        .let { (pageInfo, media) ->
            Results(
                hasNextPage = pageInfo.hasNextPage,
                items = media.map { it.toMedia() }
            )
        }
}