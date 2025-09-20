package com.mrboomdev.awery.extension.bundled.anilist.query

import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistResponse

interface AnilistQuery<T> {
    fun toGraphQLQuery(): String
    fun getResult(response: AnilistResponse): T
}