package com.mrboomdev.awery.extension.bundled.anilist.entity

import com.mrboomdev.awery.extension.bundled.anilist.query.AnilistQuery
import kotlinx.serialization.Serializable

@Serializable
data class AnilistRequest(val query: String) {
    constructor(query: AnilistQuery<*>): this(query.toGraphQLQuery())
}