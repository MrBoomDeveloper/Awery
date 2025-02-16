package eu.kanade.tachiyomi.animesource.model

import com.mrboomdev.awery.AndroidUri
import com.mrboomdev.awery.utils.ExtensionSdk
import okhttp3.Headers

@ExtensionSdk
data class Track(val url: String, val lang: String)

@ExtensionSdk
data class Video(
	val url: String,
	val quality: String,
	var videoUrl: String?,
	val headers: Headers? = null,
	val subtitleTracks: List<Track> = emptyList(),
	val audioTracks: List<Track> = emptyList(),
) {
	@ExtensionSdk
    constructor(
		url: String,
		quality: String,
		videoUrl: String?,
		@Suppress("UNUSED_PARAMETER") uri: AndroidUri? = null,
		headers: Headers? = null,
    ) : this(url, quality, videoUrl, headers)
}