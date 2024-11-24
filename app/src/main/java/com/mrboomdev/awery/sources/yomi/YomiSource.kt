package com.mrboomdev.awery.sources.yomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.ext.source.Source

abstract class YomiSource(
	packageInfo: PackageInfo
): Source() {
	final override val id = packageInfo.packageName
	abstract val feeds: CatalogSearchResults<CatalogFeed>?

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