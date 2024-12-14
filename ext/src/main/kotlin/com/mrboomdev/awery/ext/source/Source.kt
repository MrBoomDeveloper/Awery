package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.data.CatalogComment
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogFile
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting

/**
 * Source is the provider of feeds, media, subtitles and more...
 * You MUST create an constructor with the context as an argument,
 * otherwise your extension won't be loaded!
 * An context will be created by the manager,
 * so that you won't need to repeat same values multiple times.
 */
abstract class Source(
	final override val context: Context.SourceContext
): AbstractSource() {
	open suspend fun getFeeds(): CatalogSearchResults<CatalogFeed> {
		throw NotImplementedError("getFeeds() isn't implemented!")
	}

	open suspend fun getFilters(catalog: Catalog<*>): List<Setting> {
		throw NotImplementedError("getFilters() isn't implemented!")
	}

	open suspend fun <E, T: Catalog<E>> search(catalog: T, filters: List<Setting>): CatalogSearchResults<E> {
		throw NotImplementedError("search() isn't implemented!")
	}

	open suspend fun <E, T: Catalog<E>> submit(catalog: T, filters: List<Setting>): CatalogSearchResults<E> {
		throw NotImplementedError("submit() isn't implemented!")
	}

	sealed class Catalog<T> {
		data object Comment: Catalog<CatalogComment>()
		data object Tracking: Catalog<Map<String, Any>>()
		data object Media: Catalog<CatalogMedia>()
		data object Subtitles: Catalog<CatalogFile>()
	}

	companion object {
		const val FILTER_EPISODE = "__EPISODE__"
		const val FILTER_MEDIA = "__MEDIA__"
		const val FILTER_FEED = "__FEED__"
		const val FILTER_PAGE = "__PAGE__"
		const val FILTER_QUERY = "__QUERY__"
		const val FILTER_TAG = "__TAG__"

		const val TRACKING_MEDIA = "__MEDIA__"
	}
}