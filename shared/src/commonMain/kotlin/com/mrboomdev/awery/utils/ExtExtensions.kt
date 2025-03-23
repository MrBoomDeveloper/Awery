package com.mrboomdev.awery.utils

import com.mrboomdev.awery.ext.data.CatalogMedia

val CatalogMedia.bannerOrPoster 
	get() = extras[CatalogMedia.EXTRA_BANNER] ?: extras[CatalogMedia.EXTRA_POSTER]

val CatalogMedia.posterOrBanner 
	get() = extras[CatalogMedia.EXTRA_POSTER] ?: extras[CatalogMedia.EXTRA_BANNER]