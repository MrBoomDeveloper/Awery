package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.core.utils.PlatformSdk
import java.io.Serializable

@PlatformSdk
interface SAnime: Serializable {
    @PlatformSdk
    var url: String

    @PlatformSdk
    var title: String

    @PlatformSdk
    var artist: String?

    @PlatformSdk
    var author: String?

    @PlatformSdk
    var description: String?

    @PlatformSdk
    var genre: String?

    @PlatformSdk
    var status: Int

    @Suppress("PropertyName")
    @PlatformSdk
    var thumbnail_url: String?

    @Suppress("PropertyName")
    @PlatformSdk
    var update_strategy: AnimeUpdateStrategy

    @PlatformSdk
    var initialized: Boolean

    fun getGenres(): List<String>? {
        if(genre.isNullOrBlank()) return null
        return genre?.split(", ")?.map { it.trim() }?.filterNot { it.isBlank() }?.distinct()
    }

    companion object {
        @PlatformSdk
        const val UNKNOWN = 0

        @PlatformSdk
        const val ONGOING = 1

        @PlatformSdk
        const val COMPLETED = 2

        @PlatformSdk
        const val LICENSED = 3

        @PlatformSdk
        const val PUBLISHING_FINISHED = 4

        @PlatformSdk
        const val CANCELLED = 5

        @PlatformSdk
        const val ON_HIATUS = 6

        @PlatformSdk
        fun create(): SAnime = SAnimeImpl()
    }
}

private class SAnimeImpl: SAnime {
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