package com.mrboomdev.awery.extension.bundled.anilist.entity

import com.mrboomdev.awery.extension.sdk.Media
import kotlinx.serialization.Serializable

@Serializable
class AnilistMedia(
    val id: Int,
    val type: Type,
    val title: Title,
    val description: String?,
    val bannerImage: String?,
    val startDate: FuzzyDate,
    val isAdult: Boolean,
    val siteUrl: String,
    val coverImage: CoverImage,
    val genres: List<String>,
    val tags: List<Tag>,
    val episodes: Int?,
    val chapters: Int?,
    val countryOfOrigin: String?
) {
    @Serializable
    data class Title(
        val english: String?,
        val romaji: String?,
        val native: String?
    )
    
    @Serializable
    data class CoverImage(
        val extraLarge: String?,
        val large: String?,
        val medium: String?
    )
    
    @Serializable
    data class Tag(
        val name: String,
        val isAdult: Boolean
    )
    
    @Serializable
    enum class Type {
        ANIME, MANGA
    }
    
    @Serializable
    enum class Status {
        FINISHED, RELEASING, NOT_YET_RELEASED, CANCELLED, HIATUS
    }
    
    @Serializable
    enum class Sort {
        START_DATE, 
        START_DATE_DESC,
        SCORE, 
        SCORE_DESC,
        POPULARITY,
        POPULARITY_DESC,
        TRENDING,
        TRENDING_DESC,
        UPDATED_AT,
        UPDATED_AT_DESC,
        SEARCH_MATCH,
        FAVOURITES,
        FAVOURITES_DESC
    }
}

fun AnilistMedia.toMedia(): Media {
    val primaryTitle = title.english ?: title.romaji ?: title.native ?: "No title"
    
    return Media(
        id = id.toString(),
        title = primaryTitle,
        description = description,
        banner = bannerImage,
        // Medium has very bad quality so we do prefer a "large" one
        poster = coverImage.let { it.large ?: it.extraLarge ?: it.medium },
        largePoster = coverImage.let { it.extraLarge ?: it.large ?: it.medium },
        ageRating = if(isAdult) "NSFW" else null,
        releaseDate = startDate.toMillis(),
        url = siteUrl,
        episodes = episodes ?: chapters,
        country = countryOfOrigin,
        tags = genres + tags.map { it.name },

        type = when(type) {
            AnilistMedia.Type.ANIME -> Media.Type.WATCHABLE
            AnilistMedia.Type.MANGA -> Media.Type.READABLE
        },

        alternativeTitles = listOfNotNull(
            title.english,
            title.romaji,
            title.native
        ).filterNot { it == primaryTitle }
    )
}