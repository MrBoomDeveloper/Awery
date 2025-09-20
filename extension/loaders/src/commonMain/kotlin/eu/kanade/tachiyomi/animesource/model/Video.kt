package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.android.Uri
import com.mrboomdev.awery.core.utils.PlatformSdk
import kotlinx.serialization.Serializable
import okhttp3.Headers
import kotlin.concurrent.Volatile

@Serializable
@PlatformSdk
data class Track(
    val url: String,
    val lang: String
)

@Serializable
enum class ChapterType {
    Opening,
    Ending,
    Recap,
    MixedOp,
    Other
}

@Serializable
data class TimeStamp(
    val start: Double,
    val end: Double,
    val name: String,
    val type: ChapterType = ChapterType.Other
)

@PlatformSdk
open class Video(
    var videoUrl: String = "",
    val videoTitle: String = "",
    val resolution: Int? = null,
    val bitrate: Int? = null,
    val headers: Headers? = null,
    val preferred: Boolean = false,
    val subtitleTracks: List<Track> = emptyList(),
    val audioTracks: List<Track> = emptyList(),
    val timestamps: List<TimeStamp> = emptyList(),
    val internalData: String = "",
    val initialized: Boolean = false,
    val videoPageUrl: String = ""
) {
    @Deprecated("Deprecated library stuff")
    val quality: String
        get() = videoTitle

    @Deprecated("Deprecated library stuff")
    val url: String
        get() = videoPageUrl

    constructor(
        url: String,
        quality: String,
        videoUrl: String?,
        headers: Headers? = null,
        subtitleTracks: List<Track> = emptyList(),
        audioTracks: List<Track> = emptyList()
    ): this(
        videoPageUrl = url,
        videoTitle = quality,
        videoUrl = videoUrl ?: "null",
        headers = headers,
        subtitleTracks = subtitleTracks,
        audioTracks = audioTracks
    )

    constructor(
        videoUrl: String = "",
        videoTitle: String = "",
        resolution: Int? = null,
        bitrate: Int? = null,
        headers: Headers? = null,
        preferred: Boolean = false,
        subtitleTracks: List<Track> = emptyList(),
        audioTracks: List<Track> = emptyList(),
        timestamps: List<TimeStamp> = emptyList(),
        internalData: String = ""
    ): this(
        videoUrl = videoUrl,
        videoTitle = videoTitle,
        resolution = resolution,
        bitrate = bitrate,
        headers = headers,
        preferred = preferred,
        subtitleTracks = subtitleTracks,
        audioTracks = audioTracks,
        timestamps = timestamps,
        internalData = internalData,
        videoPageUrl = ""
    )

    constructor(
        url: String,
        quality: String,
        videoUrl: String?,
        uri: Uri? = null,
        headers: Headers? = null
    ): this(url, quality, videoUrl, headers)

    @Transient
    @Volatile
    var status: State = State.QUEUE

    fun copy(
        videoUrl: String = this.videoUrl,
        videoTitle: String = this.videoTitle,
        resolution: Int? = this.resolution,
        bitrate: Int? = this.bitrate,
        headers: Headers? = this.headers,
        preferred: Boolean = this.preferred,
        subtitleTracks: List<Track> = this.subtitleTracks,
        audioTracks: List<Track> = this.audioTracks,
        timestamps: List<TimeStamp> = this.timestamps,
        internalData: String = this.internalData
    ): Video = Video(
        videoUrl = videoUrl,
        videoTitle = videoTitle,
        resolution = resolution,
        bitrate = bitrate,
        headers = headers,
        preferred = preferred,
        subtitleTracks = subtitleTracks,
        audioTracks = audioTracks,
        timestamps = timestamps,
        internalData = internalData
    )

    fun copy(
        videoUrl: String = this.videoUrl,
        videoTitle: String = this.videoTitle,
        resolution: Int? = this.resolution,
        bitrate: Int? = this.bitrate,
        headers: Headers? = this.headers,
        preferred: Boolean = this.preferred,
        subtitleTracks: List<Track> = this.subtitleTracks,
        audioTracks: List<Track> = this.audioTracks,
        timestamps: List<TimeStamp> = this.timestamps,
        internalData: String = this.internalData,
        initialized: Boolean = this.initialized,
        videoPageUrl: String = this.videoPageUrl
    ) = Video(
        videoUrl = videoUrl,
        videoTitle = videoTitle,
        resolution = resolution,
        bitrate = bitrate,
        headers = headers,
        preferred = preferred,
        subtitleTracks = subtitleTracks,
        audioTracks = audioTracks,
        timestamps = timestamps,
        internalData = internalData,
        videoPageUrl = videoPageUrl
    )

    enum class State {
        QUEUE,
        LOAD_VIDEO,
        READY,
        ERROR
    }
}