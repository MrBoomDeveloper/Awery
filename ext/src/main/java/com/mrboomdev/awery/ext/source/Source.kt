package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogFile
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Settings
import com.mrboomdev.awery.ext.util.Image

/**
 * Source is the provider of feeds, media, subtitles and more...
 */
abstract class Source {
	abstract val features: Array<AweryFeature>
	abstract val id: String
	abstract val name: String
	abstract val manager: SourcesManager<*>
	abstract val exception: Throwable?
	abstract val ageRating: String
	abstract val icon: Image?
	open val isEnabled: Boolean = true

	open suspend fun getFeeds(): CatalogSearchResults<CatalogFeed> {
		throw NotImplementedError("getFeeds() isn't implemented!")
	}

	open suspend fun getFilters(catalog: Catalog<*>): Settings {
		throw NotImplementedError("getFilters() isn't implemented!")
	}

	open suspend fun <E, T: Catalog<E>> search(catalog: T, filters: Settings): CatalogSearchResults<E> {
		throw NotImplementedError("search() isn't implemented!")
	}

	sealed class Catalog<T> {
		data object Media: Catalog<CatalogMedia>()
		data object Subtitles: Catalog<CatalogFile>()
	}

	companion object {
		const val FILTER_FEED = "__FEED__"
		const val FILTER_PAGE = "__PAGE__"
		const val FILTER_QUERY = "__QUERY__"
		const val FILTER_TAG = "__TAG__"
	}
}