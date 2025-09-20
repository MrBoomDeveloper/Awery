package eu.kanade.tachiyomi.network

import app.cash.quickjs.QuickJs
import com.mrboomdev.awery.core.utils.PlatformSdk
import kotlinx.coroutines.Dispatchers

@PlatformSdk
actual object JavaScriptEngine {
    @PlatformSdk
    actual suspend fun <T> evaluate(script: String): T = with(Dispatchers.IO) {
        QuickJs.create().use {
            @Suppress("UNCHECKED_CAST")
            it.evaluate(script) as T
        }
    }
}