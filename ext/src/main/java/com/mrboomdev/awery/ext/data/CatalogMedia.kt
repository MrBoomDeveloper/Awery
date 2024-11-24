package com.mrboomdev.awery.ext.data

import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.squareup.moshi.JsonClass
import java.io.Serial
import java.io.Serializable

@kotlinx.serialization.Serializable
class CatalogMedia(
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

	val tags: Array<CatalogTag>? = null,
	val genres: Array<String>? = null,
	val titles: Array<String>? = null,
	val authors: Map<String, String>? = null,
	val ids: Map<String, String>? = null
) : Serializable {

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