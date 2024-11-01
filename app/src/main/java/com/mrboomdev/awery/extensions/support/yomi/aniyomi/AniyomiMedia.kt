package com.mrboomdev.awery.extensions.support.yomi.aniyomi

import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogTag
import com.mrboomdev.awery.extensions.support.yomi.YomiProvider
import com.mrboomdev.awery.util.extensions.mapOfNotNull
import eu.kanade.tachiyomi.animesource.model.SAnime
import eu.kanade.tachiyomi.animesource.model.SAnimeImpl
import eu.kanade.tachiyomi.animesource.online.AnimeHttpSource

open class AniyomiMedia(
	provider: AniyomiProvider, protected val anime: SAnime
) : CatalogMedia(
	"${AniyomiManager.MANAGER_ID};;;${provider.id}:${provider.extension.id};;;${anime.url}",
	anime.thumbnail_url,
	anime.description,
	null,
	null,
	anime.url,

	if(provider.source !is AnimeHttpSource) null
	else YomiProvider.concatLink(provider.source.baseUrl, anime.url),

	Type.TV,
	anime.thumbnail_url,
	null,
	null,
	null,
	null,
	null,

	when(anime.status) {
		SAnime.COMPLETED, SAnime.PUBLISHING_FINISHED -> Status.COMPLETED
		SAnime.ONGOING -> Status.ONGOING
		SAnime.ON_HIATUS -> Status.PAUSED
		SAnime.CANCELLED -> Status.CANCELLED
		else -> null
	},

	anime.genre?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.map { CatalogTag(it) }
		?.toTypedArray(),

	anime.genre?.split(", ".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
		?.map { genre -> genre.trim { it <= ' ' } }
		?.filter { it.isNotBlank() }
		?.toTypedArray(),

	arrayOf(anime.title),

	mapOfNotNull(
		Pair("Author", anime.author),
		Pair("Artist", anime.artist)
	),

	null
) {
	companion object {
		@JvmStatic
		fun fromMedia(media: CatalogMedia): SAnime {
			if(media is AniyomiMedia) {
				return media.anime
			}

			val anime = SAnimeImpl()
			anime.title = media.title ?: "No title"
			anime.description = media.description
			anime.thumbnail_url = media.poster
			anime.url = media.extra!!

			if(media.authors != null) {
				anime.author = media.authors!!["Author"]
				anime.artist = media.authors!!["Artist"]
			}

			anime.status = when(media.status) {
				Status.ONGOING -> SAnime.ONGOING
				Status.COMPLETED -> SAnime.COMPLETED
				Status.PAUSED -> SAnime.ON_HIATUS
				Status.CANCELLED -> SAnime.CANCELLED
				else -> 0
			}

			anime.genre = media.genres
				?.map { genre -> genre.trim { it <= ' ' } }
				?.filter { it.isNotBlank() }
				?.joinToString(", ")

			return anime
		}
	}
}