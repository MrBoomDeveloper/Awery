package com.mrboomdev.awery.data

enum class AgeRating {
    EVERYONE,
    NSFW;

    companion object {
        fun of(string: String): AgeRating? {
            return when(string.lowercase().trim()) {
                "0+", "+0" -> EVERYONE
                "nsfw", "18+", "+18" -> NSFW
                else -> null
            }
        }
    }
}