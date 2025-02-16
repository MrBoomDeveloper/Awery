package com.mrboomdev.awery.sources.yomi.aniyomi

import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.util.createGlobalId
import com.mrboomdev.awery.sources.yomi.YomiSource
import com.mrboomdev.awery.utils.arrayOfNotNull
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource

private const val EXTRA_ANIYOMI_URL = "ANIYOMI_URL"

fun SAnime.toMedia(source: AniyomiSource) = CatalogMedia.create(
	globalId = source.createGlobalId(url),
	type = "tv",
	title = title,
	poster = thumbnail_url,
	description = description,
	extras = mapOf(EXTRA_ANIYOMI_URL to url),
	authors = arrayOfNotNull(author, artist),
	
	shareUrl = if(source.source !is AnimeHttpSource) null
	else YomiSource.concatLink(source.source.baseUrl, url),

	status = when(status) {
		SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> CatalogMedia.Status.COMPLETED
		SAnime.ONGOING -> CatalogMedia.Status.ONGOING
		SAnime.ON_HIATUS -> CatalogMedia.Status.PAUSED
		SAnime.CANCELLED -> CatalogMedia.Status.CANCELLED
		else -> null
	},

	genres = genre?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.toTypedArray()
)

fun CatalogMedia.toSAnime() = SAnime.create().apply {
	title = this@toSAnime.title
	description = extras[CatalogMedia.EXTRA_DESCRIPTION]
	thumbnail_url = extras[CatalogMedia.EXTRA_POSTER] ?: extras[CatalogMedia.EXTRA_BANNER]
	url = extras[EXTRA_ANIYOMI_URL] ?: globalId.itemId!!

	status = when(this@toSAnime.status) {
		CatalogMedia.Status.ONGOING -> SAnime.ONGOING
		CatalogMedia.Status.COMPLETED -> SAnime.COMPLETED
		CatalogMedia.Status.PAUSED -> SAnime.ON_HIATUS
		CatalogMedia.Status.CANCELLED -> SAnime.CANCELLED
		else -> 0
	}

	genre = this@toSAnime.genres
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.joinToString(", ")
}