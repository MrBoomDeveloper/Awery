package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Settings

abstract class StubSource: Source() {
	override val isEnabled = false

	override suspend fun getFeeds(): CatalogSearchResults<CatalogFeed> {
		throw UnsupportedOperationException("Source isn't loaded, so this method cannot be invoked!")
	}

	override suspend fun getFilters(catalog: Catalog<*>): Settings {
		throw UnsupportedOperationException("Source isn't loaded, so this method cannot be invoked!")
	}

	override suspend fun <E, T : Catalog<E>> search(catalog: T, filters: Settings): CatalogSearchResults<E> {
		throw UnsupportedOperationException("Source isn't loaded, so this method cannot be invoked!")
	}
}