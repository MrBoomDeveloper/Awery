package com.mrboomdev.awery.extension.sdk

import kotlinx.serialization.Serializable

@Serializable
data class Media(
    val id: String,
    val title: String,
    val alternativeTitles: List<String> = emptyList(),
    val poster: String? = null,
    val largePoster: String? = null,
    val banner: String? = null,
    val description: String? = null,
    val url: String? = null,
    val releaseDate: Long? = null,
    val country: String? = null,
    val episodes: Int? = null,
    val tags: List<String>? = null,
    val ageRating: String? = null,
    val type: Type = Type.WATCHABLE,
    val extras: Map<String, String> = emptyMap()
) {
    @Serializable
    enum class Type {
        WATCHABLE, READABLE
    }
}