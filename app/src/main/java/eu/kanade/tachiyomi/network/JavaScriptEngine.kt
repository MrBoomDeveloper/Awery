package eu.kanade.tachiyomi.network

import app.cash.quickjs.QuickJs
import tachiyomi.core.util.lang.withIOContext

@Suppress("UNCHECKED_CAST", "unused")
class JavaScriptEngine {

    suspend fun <T> evaluate(script: String): T = withIOContext {
        QuickJs.create().use {
            it.evaluate(script) as T
        }
    }
}