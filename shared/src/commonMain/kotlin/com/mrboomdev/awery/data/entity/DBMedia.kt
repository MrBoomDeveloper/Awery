package com.mrboomdev.awery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogTag
import com.mrboomdev.awery.utils.toEnum

@Suppress("ArrayInDataClass")
@Entity
internal data class DBMedia(
	@PrimaryKey val globalId: String,
	val banner: String?,
	val description: String?,
	val country: String?,
	val ageRating: String?,
	val extra: String?,
	val url: String?,
	val type: String?,
	val poster: String?,
	val releaseDate: Long?,
	val duration: Int?,
	val episodesCount: Int?,
	val latestEpisode: Int?,
	val averageScore: Float?,
	val status: String?,
	val tags: Array<Map<String, String>>?,
	val genres: Array<String>?,
	val titles: Array<String>?,
	val authors: Map<String, String>?,
	val ids: Map<String, String>?
) {
	fun asCatalogMedia() = CatalogMedia(
		globalId,
		banner,
		description,
		country,
		ageRating?.toEnum<AweryAgeRating>(),
		extra,
		url,
		type?.toEnum<CatalogMedia.Type>(),
		poster,
		releaseDate,
		duration,
		episodesCount,
		latestEpisode,
		averageScore,
		status?.toEnum<CatalogMedia.Status>(),
		
		tags?.map { CatalogTag(
			it["name"]!!, 
			it["description"], 
			it["isAdult"]?.toBoolean() == true, 
			it["isSpoiler"]?.toBoolean() == true)
		}?.toTypedArray(),
		
		genres,
		titles,
		authors,
		ids,
	)
}

internal fun CatalogMedia.asDBMedia() = DBMedia(
	globalId,
	banner,
	description,
	country,
	ageRating?.name,
	extra,
	url,
	type?.name,
	poster,
	releaseDate,
	duration,
	episodesCount,
	latestEpisode,
	averageScore,
	status?.name,
	
	tags?.map { buildMap { 
		put("name", it.name)
		it.description?.also { put("description", it) }
		put("isAdult", it.isAdult.toString())
		put("isSpoiler", it.isSpoiler.toString())
	}}?.toTypedArray(),
	
	genres,
	titles,
	authors,
	ids,
)