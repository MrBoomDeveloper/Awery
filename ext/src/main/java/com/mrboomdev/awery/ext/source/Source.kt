package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Settings
import com.mrboomdev.awery.ext.util.Image

abstract class Source {
	abstract val features: Array<AweryFeature>
	abstract val id: String
	abstract val name: String
	abstract val manager: SourcesManager<*>
	abstract val exception: Throwable?
	abstract val ageRating: String
	abstract val icon: Image?

	open suspend fun getFeeds(): CatalogSearchResults<CatalogFeed> {
		throw NotImplementedError("getFeeds() isn't implemented!")
	}

	open suspend fun getSearchMediaFilters(): Settings {
		throw NotImplementedError("getSearchMediaFilters() isn't implemented!")
	}

	open suspend fun searchMedia(filters: Settings): CatalogSearchResults<CatalogMedia> {
		throw NotImplementedError("searchMedia() isn't implemented!")
	}
}