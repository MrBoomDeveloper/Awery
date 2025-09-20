package com.mrboomdev.awery.extension.bundled.anilist.query

import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistMedia
import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistResponse
import com.mrboomdev.awery.extension.sdk.Media

class MediaQuery(
	private val id: String
): AnilistQuery<AnilistMedia> {
	override fun toGraphQLQuery() = """
		query {
			Media(id: $id) {
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
		}
	""".trimIndent()

	override fun getResult(response: AnilistResponse): AnilistMedia {
		return response.data!!.Media!!
	}
}