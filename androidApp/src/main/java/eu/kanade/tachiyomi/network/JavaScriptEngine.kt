package eu.kanade.tachiyomi.network

import app.cash.quickjs.QuickJs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Suppress("UNCHECKED_CAST", "unused")
class JavaScriptEngine {

    suspend fun <T> evaluate(script: String): T = withContext(Dispatchers.IO) {
        QuickJs.create().use {
            it.evaluate(script) as T
        }
    }
}