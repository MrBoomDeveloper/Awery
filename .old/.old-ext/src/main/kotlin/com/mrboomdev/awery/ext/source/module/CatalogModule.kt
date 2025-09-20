package com.mrboomdev.awery.ext.source.module

import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.data.Setting

interface CatalogModule: Module {
	suspend fun createFeeds(): CatalogSearchResults<CatalogFeed>
	suspend fun search(filters: List<Setting>): CatalogSearchResults<CatalogMedia>
}