package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.core.utils.PlatformSdk

/**
 * A source that explicitly doesn't require traffic considerations.
 *
 * This typically applies for self-hosted sources.
 */
@PlatformSdk
interface UnmeteredSource