package eu.kanade.tachiyomi.animesource.model

import android.net.Uri
import okhttp3.Headers

data class Track(val url: String, val lang: String)

@Suppress("UNUSED_PARAMETER")
data class Video(
    val url: String,
    val quality: String,
    var videoUrl: String?,
    val headers: Headers? = null,
    val subtitleTracks: List<Track> = emptyList(),
    val audioTracks: List<Track> = emptyList(),
) {
    constructor(
        url: String,
        quality: String,
        videoUrl: String?,
        uri: Uri? = null,
        headers: Headers? = null,
    ) : this(url, quality, videoUrl, headers)
}