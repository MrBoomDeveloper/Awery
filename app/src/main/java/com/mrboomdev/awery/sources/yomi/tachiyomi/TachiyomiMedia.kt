package com.mrboomdev.awery.sources.yomi.tachiyomi

import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogTag
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider
import com.mrboomdev.awery.util.extensions.mapOfNotNull
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaImpl
import eu.kanade.tachiyomi.source.online.HttpSource

fun SManga.toMedia(source: TachiyomiSource) = CatalogMedia(
	"${TachiyomiManager.ID};;;${source.id};;;${url}",
	thumbnail_url,
	description,
	null,
	null,
	url,

	if(source.source !is HttpSource) null
	else YomiProvider.concatLink(source.source.baseUrl, url),

	CatalogMedia.Type.BOOK,
	thumbnail_url,
	null,
	null,
	null,
	null,
	null,

	when(status) {
		SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> CatalogMedia.Status.COMPLETED
		SAnime.ONGOING -> CatalogMedia.Status.ONGOING
		SAnime.ON_HIATUS -> CatalogMedia.Status.PAUSED
		SAnime.CANCELLED -> CatalogMedia.Status.CANCELLED
		else -> null
	},

	genre?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.map { CatalogTag(it) }
		?.toTypedArray(),

	genre?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.toTypedArray(),

	arrayOf(title),

	mapOfNotNull(
		Pair("Author", author),
		Pair("Artist", artist)
	),

	null
)

fun CatalogMedia.toSManga(): SManga {
	return SMangaImpl().also { manga ->
		manga.title = title ?: "No title"
		manga.description = description
		manga.thumbnail_url = poster
		manga.url = extra!!

		if(authors != null) {
			manga.author = authors!!["Author"]
			manga.artist = authors!!["Artist"]
		}

		manga.status = when(status) {
			CatalogMedia.Status.ONGOING -> SAnime.ONGOING
			CatalogMedia.Status.COMPLETED -> SAnime.COMPLETED
			CatalogMedia.Status.PAUSED -> SAnime.ON_HIATUS
			CatalogMedia.Status.CANCELLED -> SAnime.CANCELLED
			else -> 0
		}

		manga.genre = genres
			?.map { genre -> genre.trim { it <= ' ' } }
			?.filter { it.isNotBlank() }
			?.joinToString(", ")
	}
}