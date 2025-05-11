package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.utils.ExtensionSdk

/**
 * A source that explicitly doesn't require traffic considerations.
 *
 * This typically applies for self-hosted sources.
 */
@ExtensionSdk
interface UnmeteredSource