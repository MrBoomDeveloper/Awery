package com.mrboomdev.awery.sources.yomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.source.Context
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.util.Image

abstract class YomiSource(
	manager: YomiManager<*>,
	name: String,
	ageRating: AgeRating?,
	features: Array<AweryFeature>,
	packageInfo: PackageInfo,
	isEnabled: Boolean,
	exception: Throwable?,
	icon: Image
): Source() {
	abstract val feeds: CatalogSearchResults<CatalogFeed>?

	init {
		attachContext(object : Context.SourceContext {
			override val manager = manager
			override val ageRating = ageRating
			override val features = features
			override val id = packageInfo.packageName
			override val isEnabled = isEnabled
			override val name = name
			override val exception = exception
			override val icon = icon
		})
	}
	
	final override suspend fun getFeeds(): CatalogSearchResults<CatalogFeed> {
		return feeds!!
	}

	companion object {
		const val FEED_POPULAR = "popular"
		const val FEED_LATEST = "latest"

		fun concatLink(domain: String, path: String): String {
			var mDomain = domain
			var mPath = path

			if(mDomain.endsWith("/")) {
				mDomain = mDomain.substring(0, mDomain.length - 1)
			}

			if(mPath.startsWith("/")) {
				mPath = mPath.substring(1)
			}

			return "$mDomain/$mPath"
		}
	}
}