package com.mrboomdev.awery.util.adapters

import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogTag
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

object MediaAdapter {

	@ToJson
	fun toJson(media: CatalogMedia): MoshiMedia {
		return MoshiMedia(
			globalId = media.globalId,
			titles = media.titles,
			ids = media.ids,
			authors = media.authors,
			banner = media.banner,
			description = media.description,
			country = media.country,
			ageRating = media.ageRating,
			extra = media.extra,
			url = media.url,
			type = media.type,
			releaseDate = media.releaseDate,
			duration = media.duration,
			episodesCount = media.episodesCount,
			latestEpisode = media.latestEpisode,
			averageScore = media.averageScore,
			tags = media.tags,
			genres = media.genres,
			status = media.status
		)
	}

	@FromJson
	fun fromJson(media: MoshiMedia): CatalogMedia {
		return CatalogMedia(
			globalId = media.globalId,
			titles = media.titles,
			ids = media.ids,
			authors = media.authors,
			banner = media.banner,
			description = media.description,
			country = media.country,
			ageRating = media.ageRating,
			extra = media.extra,
			url = media.url,
			type = media.type,
			releaseDate = media.releaseDate,
			duration = media.duration,
			episodesCount = media.episodesCount,
			latestEpisode = media.latestEpisode,
			averageScore = media.averageScore,
			tags = media.tags,
			genres = media.genres,
			status = media.status
		)
	}

	data class MoshiMedia(
		var globalId: String,
		var titles: Array<String>? = null,
		var ids: Map<String, String>? = null,
		var authors: Map<String, String>? = null,
		var banner: String? = null,
		var description: String? = null,
		var country: String? = null,
		var ageRating: AweryAgeRating? = null,
		var extra: String? = null,
		var url: String? = null,
		var type: CatalogMedia.Type? = null,
		var releaseDate: Long? = null,
		var duration: Int? = null,
		var episodesCount: Int? = null,
		var latestEpisode: Int? = null,
		var averageScore: Float? = null,
		var tags: Array<CatalogTag>? = null,
		var genres: Array<String>? = null,
		var status: CatalogMedia.Status? = null,
		var poster: String? = null,
		var smallPoster: String? = null
	) {
		override fun equals(other: Any?): Boolean {
			if(this === other) return true
			if(javaClass != other?.javaClass) return false

			other as MoshiMedia

			if(globalId != other.globalId) return false

			if(titles != null) {
				if(other.titles == null) return false
				if(!titles.contentEquals(other.titles)) return false
			} else if(other.titles != null) return false

			if(ids != other.ids) return false
			if(authors != other.authors) return false
			if(banner != other.banner) return false
			if(description != other.description) return false
			if(country != other.country) return false
			if(ageRating != other.ageRating) return false
			if(extra != other.extra) return false
			if(url != other.url) return false
			if(type != other.type) return false
			if(releaseDate != other.releaseDate) return false
			if(duration != other.duration) return false
			if(episodesCount != other.episodesCount) return false
			if(latestEpisode != other.latestEpisode) return false
			if(averageScore != other.averageScore) return false

			if(tags != null) {
				if(other.tags == null) return false
				if(!tags.contentEquals(other.tags)) return false
			} else if(other.tags != null) return false

			if(genres != null) {
				if(other.genres == null) return false
				if(!genres.contentEquals(other.genres)) return false
			} else if(other.genres != null) return false

			if(status != other.status) return false
			if(poster != other.poster) return false
			if(smallPoster != other.smallPoster) return false

			return true
		}

		override fun hashCode(): Int {
			var result = globalId.hashCode()
			result = 31 * result + (titles?.contentHashCode() ?: 0)
			result = 31 * result + ids.hashCode()
			result = 31 * result + (authors?.hashCode() ?: 0)
			result = 31 * result + (banner?.hashCode() ?: 0)
			result = 31 * result + (description?.hashCode() ?: 0)
			result = 31 * result + (country?.hashCode() ?: 0)
			result = 31 * result + (ageRating?.hashCode() ?: 0)
			result = 31 * result + (extra?.hashCode() ?: 0)
			result = 31 * result + (url?.hashCode() ?: 0)
			result = 31 * result + (type?.hashCode() ?: 0)
			result = 31 * result + (releaseDate?.hashCode() ?: 0)
			result = 31 * result + (duration ?: 0)
			result = 31 * result + (episodesCount ?: 0)
			result = 31 * result + (latestEpisode ?: 0)
			result = 31 * result + (averageScore?.hashCode() ?: 0)
			result = 31 * result + (tags?.contentHashCode() ?: 0)
			result = 31 * result + (genres?.contentHashCode() ?: 0)
			result = 31 * result + (status?.hashCode() ?: 0)
			result = 31 * result + (poster?.hashCode() ?: 0)
			result = 31 * result + (smallPoster?.hashCode() ?: 0)
			return result
		}
	}
}