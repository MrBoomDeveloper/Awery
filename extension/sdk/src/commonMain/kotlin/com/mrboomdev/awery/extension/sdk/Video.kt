package com.mrboomdev.awery.extension.sdk

import kotlinx.serialization.Serializable

@Serializable
data class Video(
    val url: String,
    val title: String? = null,
    val videoTracks: List<Track> = emptyList(),
    val subtitleTracks: List<Track> = emptyList(),
    val audioTracks: List<Track> = emptyList(),
    val headers: Map<String, String> = emptyMap()
) {
    @Serializable
    data class Track(
        val url: String,
        val title: String? = null,
        val locale: String? = null
    )
}