package com.mrboomdev.awery.extension.sdk

data class WatchVariant(
    val id: String,
    val number: Float? = null,
    val title: String,
    val type: Type? = null,
    val releaseDate: Long? = null
) {
    enum class Type {
        LOCALE, SEASON, QUALITY, EPISODE
    }
}