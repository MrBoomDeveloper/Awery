package com.mrboomdev.awery.extension.sdk

import kotlinx.serialization.Serializable

@Serializable
data class Results<T>(
    val items: List<T>,
    val hasNextPage: Boolean
)