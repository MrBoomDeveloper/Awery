package com.mrboomdev.awery.sources.yomi.tachiyomi

import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.util.createGlobalId
import com.mrboomdev.awery.sources.yomi.YomiSource
import com.mrboomdev.awery.utils.arrayOfNotNull
import com.mrboomdev.awery.utils.genres
import com.mrboomdev.awery.utils.status
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.online.HttpSource

private const val EXTRA_TACHIYOMI_URL = "TACHIYOMI_URL"

fun SManga.toMedia(source: TachiyomiSource) = CatalogMedia.create(
	globalId = source.createGlobalId(url),
	type = "comic",
	title = title,
	poster = thumbnail_url,
	description = description,
	extras = mapOf(EXTRA_TACHIYOMI_URL to url),
	authors = arrayOfNotNull(author, artist),
	
	shareUrl = if(source.source !is HttpSource) null
	else YomiSource.concatLink(source.source.baseUrl, url),
	
	status = when(status) {
		SManga.COMPLETED, SManga.PUBLISHING_FINISHED -> CatalogMedia.Status.COMPLETED
		SManga.ONGOING -> CatalogMedia.Status.ONGOING
		SManga.ON_HIATUS -> CatalogMedia.Status.PAUSED
		SManga.CANCELLED -> CatalogMedia.Status.CANCELLED
		else -> null
	},
	
	genres = genre?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.toTypedArray()
)

fun CatalogMedia.toSManga() = SManga.create().apply {
	title = this@toSManga.title
	description = extras[CatalogMedia.EXTRA_DESCRIPTION]
	thumbnail_url = extras[CatalogMedia.EXTRA_POSTER] ?: extras[CatalogMedia.EXTRA_BANNER]
	url = extras[EXTRA_TACHIYOMI_URL] ?: globalId.itemId!!
	
	status = when(this@toSManga.status) {
		CatalogMedia.Status.ONGOING -> SAnime.ONGOING
		CatalogMedia.Status.COMPLETED -> SAnime.COMPLETED
		CatalogMedia.Status.PAUSED -> SAnime.ON_HIATUS
		CatalogMedia.Status.CANCELLED -> SAnime.CANCELLED
		else -> 0
	}
	
	genre = this@toSManga.genres
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.joinToString(", ")
}