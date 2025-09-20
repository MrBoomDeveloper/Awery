package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.core.utils.PlatformSdk
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.Dispatchers

@PlatformSdk
actual object JavaScriptEngine {
    @PlatformSdk
    actual suspend fun <T> evaluate(script: String): T = with(Dispatchers.IO) {
        QuickJSContext.create().use {
            @Suppress("UNCHECKED_CAST")
            it.evaluate(script) as T
        }
    }
}