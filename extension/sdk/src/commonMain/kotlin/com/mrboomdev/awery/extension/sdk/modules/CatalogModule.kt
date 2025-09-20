package com.mrboomdev.awery.extension.sdk.modules

import com.mrboomdev.awery.extension.sdk.Feed
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Results

interface CatalogModule: Module {
    suspend fun getDefaultFilters(): List<Preference<*>>
    suspend fun search(filters: List<Preference<*>>, page: Int = 0): Results<Media>
    suspend fun updateMedia(media: Media): Media = media
    suspend fun getFeeds(page: Int = 0): Results<Feed>
    suspend fun loadFeed(feed: Feed, page: Int = 0): Results<Media>
}