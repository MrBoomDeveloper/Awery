package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.core.utils.PlatformSdk
import java.io.Serializable

@PlatformSdk
interface SEpisode: Serializable {
    @PlatformSdk
    var url: String

    @PlatformSdk
    var name: String

    @Suppress("PropertyName")
    @PlatformSdk
    var date_upload: Long

    @Suppress("PropertyName")
    @PlatformSdk
    var episode_number: Float

    @PlatformSdk
    var scanlator: String?

    companion object {
        @PlatformSdk
        fun create(): SEpisode = SEpisodeImpl()
    }
}

private class SEpisodeImpl: SEpisode {
    override lateinit var url: String
    override lateinit var name: String
    override var date_upload: Long = 0
    override var episode_number: Float = -1F
    override var scanlator: String? = null
}