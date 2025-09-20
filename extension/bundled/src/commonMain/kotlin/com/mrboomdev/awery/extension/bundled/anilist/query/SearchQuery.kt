package com.mrboomdev.awery.extension.bundled.anilist.query

import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistMedia
import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistPage
import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistResponse
import com.mrboomdev.awery.extension.bundled.anilist.entity.FuzzyDateInt
import com.mrboomdev.awery.extension.bundled.anilist.graphqlParams

data class SearchQuery(
    val page: Int,
    val perPage: Int = 25,
    val search: String? = null,
    val isAdult: Boolean? = null,
    val sort: AnilistMedia.Sort? = null,
    val type: AnilistMedia.Type? = null,
    val startDate: FuzzyDateInt? = null,
    val endDate: FuzzyDateInt? = null,
    val status: AnilistMedia.Status? = null
): AnilistQuery<Pair<AnilistPage.Info, List<AnilistMedia>>> {
    override fun toGraphQLQuery() = """
        query {
            Page(
                page: $page, 
                perPage: $perPage
            ) {
                media(${graphqlParams(
                    "search" to search?.let { it.ifBlank { null } },
                    "sort" to sort,
                    "status" to status,
                    "isAdult" to isAdult,
                    "type" to type,
                    "startDate" to startDate?.value,
                    "endDate" to endDate?.value
                )}) {
                    id
                    type
                    description
                    isAdult
                    siteUrl
                    bannerImage
                    genres
                    episodes
                    chapters
                    countryOfOrigin
                    
                    startDate {
                        year month day
                    }
                    
                    tags {
                        name isAdult
                    }
                    
                    coverImage {
                        medium large extraLarge
                    }
                    
                    title {
                        english romaji native
                    }
                }
                
                pageInfo {
                    hasNextPage
                }
            }
        }
    """.trimIndent()

    override fun getResult(response: AnilistResponse): Pair<AnilistPage.Info, List<AnilistMedia>> {
        return response.data!!.Page!!.let { it.pageInfo to it.media!! }
    }
}