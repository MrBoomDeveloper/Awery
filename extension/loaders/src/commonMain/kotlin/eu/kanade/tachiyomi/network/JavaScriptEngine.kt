package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.core.utils.PlatformSdk

/**
 * Util for evaluating JavaScript in sources.
 */
@Suppress("UNUSED", "UNCHECKED_CAST")
@PlatformSdk
expect object JavaScriptEngine {

    /**
     * Evaluate arbitrary JavaScript code and get the result as a primtive type
     * (e.g., String, Int).
     *
     * @since extensions-lib 1.4
     * @param script JavaScript to execute.
     * @return Result of JavaScript code as a primitive type.
     */
    @PlatformSdk
    suspend fun <T> evaluate(script: String): T
}