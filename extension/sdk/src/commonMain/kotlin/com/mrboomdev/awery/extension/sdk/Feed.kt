package com.mrboomdev.awery.extension.sdk

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val id: String,
    val name: String
)