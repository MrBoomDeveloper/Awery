package com.mrboomdev.awery.ext.data

import com.mrboomdev.awery.ext.constants.AweryAgeRating
import java.io.Serial
import java.io.Serializable

open class CatalogMedia(
	val globalId: String,

	val banner: String? = null,
	val description: String? = null,
	val country: String? = null,
	val ageRating: AweryAgeRating? = null,
	val extra: String? = null,
	val url: String? = null,
	val type: Type? = null,
	val poster: String? = null,
	val releaseDate: Long? = null,
	val duration: Int? = null,
	val episodesCount: Int? = null,
	val latestEpisode: Int? = null,
	val averageScore: Float? = null,
	val status: Status? = null,

	tags: Array<CatalogTag>? = null,
	genres: Array<String>? = null,
	titles: Array<String>? = null,
	authors: Map<String, String>? = null,
	ids: Map<String, String>? = null
) : Serializable {
	val ids = ids?.toMap()
	val authors = authors?.toMap()
	val titles = titles?.clone()
	val tags = tags?.clone()
	val genres = genres?.clone()

	constructor(original: CatalogMedia) : this(
		globalId = original.globalId,

		banner = original.banner,
		description = original.description,
		country = original.country,
		ageRating = original.ageRating,
		extra = original.extra,
		url = original.url,
		type = original.type,
		poster = original.poster,
		releaseDate = original.releaseDate,
		duration = original.duration,
		episodesCount = original.episodesCount,
		latestEpisode = original.latestEpisode,
		averageScore = original.averageScore,
		status = original.status,

		tags = original.tags,
		genres = original.genres,
		titles = original.titles,
		authors = original.authors,
		ids = original.ids
	)

	val id: String
		get() = globalId.split(";;;".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]

	val title: String?
		get() = titles?.getOrNull(0)

	fun getId(type: String): String? {
		return ids?.get(type)
	}

	enum class Status {
		ONGOING, COMPLETED, COMING_SOON, PAUSED, CANCELLED
	}

	enum class Type {
		MOVIE, BOOK, TV, POST
	}

	companion object {
		@Serial
		private val serialVersionUID: Long = 1
	}
}