package com.mrboomdev.awery.extension.bundled.anilist.entity

import kotlinx.serialization.Serializable

@Suppress("PropertyName")
@Serializable
data class AnilistResponse(
    val data: Data?
) {
    @Serializable
    data class Data(
        val Page: AnilistPage? = null,
        val Media: AnilistMedia? = null
    )
}

@Serializable
data class AnilistPage(
    val pageInfo: Info,
    val media: List<AnilistMedia>?
) {
    @Serializable
    data class Info(
        val hasNextPage: Boolean
    )
}