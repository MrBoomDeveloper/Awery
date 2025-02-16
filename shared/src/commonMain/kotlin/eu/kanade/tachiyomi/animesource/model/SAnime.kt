@file:Suppress("PropertyName", "PropertyName", "PropertyName", "PropertyName")

package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.utils.ExtensionSdk
import java.io.Serializable

@ExtensionSdk
interface SAnime : Serializable {
    var url: String
    var title: String
    var artist: String?
    var author: String?
    var description: String?
    var genre: String?
    var status: Int
    var thumbnail_url: String?
    var update_strategy: AnimeUpdateStrategy
    var initialized: Boolean
    
    companion object {
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        const val PUBLISHING_FINISHED = 4
        const val CANCELLED = 5
        const val ON_HIATUS = 6

        @ExtensionSdk
        fun create(): SAnime = object : SAnime {
            override lateinit var url: String
            override lateinit var title: String
            override var artist: String? = null
            override var author: String? = null
            override var description: String? = null
            override var genre: String? = null
            override var status: Int = 0
            override var thumbnail_url: String? = null
            override var initialized: Boolean = false
            override var update_strategy: AnimeUpdateStrategy = AnimeUpdateStrategy.ALWAYS_UPDATE
        }
    }
}