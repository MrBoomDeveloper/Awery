package com.mrboomdev.awery.extension.bundled

import com.mrboomdev.awery.extension.bundled.anilist.AnilistExtension
import com.mrboomdev.awery.extension.sdk.Context
import com.mrboomdev.awery.extension.sdk.Extension

/**
 * A way to maintain extensions while proper sdk isn't done yet.
 * Any fork may insert here their own bundled extensions.
 */
object BundledExtensions {
    fun getAll(
        contextFactory: (extensionId: String) -> Context
    ): Collection<Extension> = listOf(
        AnilistExtension(contextFactory(AnilistExtension.ID))
    )
}