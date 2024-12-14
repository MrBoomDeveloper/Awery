package com.mrboomdev.awery.sources.yomi.aniyomi

import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.util.createGlobalId
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider
import com.mrboomdev.awery.util.extensions.mapOfNotNull
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource

fun SAnime.toMedia(source: AniyomiSource) = CatalogMedia(
	globalId = source.createGlobalId(url),
	type = CatalogMedia.Type.TV,
	titles = arrayOf(title),
	poster = thumbnail_url,
	description = description,
	extra = url,

	authors = mapOfNotNull(
		Pair("Author", author),
		Pair("Artist", artist)
	),

	url = if(source.source !is AnimeHttpSource) null
	else YomiProvider.concatLink(source.source.baseUrl, url),

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

fun CatalogMedia.toSAnime(): SAnime {
	return SAnimeImpl().also { anime ->
		anime.title = title ?: "No title"
		anime.description = description
		anime.thumbnail_url = poster
		anime.url = extra!!

		if(authors != null) {
			anime.author = authors!!["Author"]
			anime.artist = authors!!["Artist"]
		}

		anime.status = when(status) {
			CatalogMedia.Status.ONGOING -> SAnime.ONGOING
			CatalogMedia.Status.COMPLETED -> SAnime.COMPLETED
			CatalogMedia.Status.PAUSED -> SAnime.ON_HIATUS
			CatalogMedia.Status.CANCELLED -> SAnime.CANCELLED
			else -> 0
		}

		anime.genre = genres
			?.map { genre -> genre.trim { it <= ' ' } }
			?.filter { it.isNotBlank() }
			?.joinToString(", ")
	}
}