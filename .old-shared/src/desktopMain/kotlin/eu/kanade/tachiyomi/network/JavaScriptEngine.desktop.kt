package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.AndroidContext
import com.mrboomdev.awery.utils.ExtensionSdk
import com.whl.quickjs.wrapper.QuickJSContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExtensionSdk
actual class JavaScriptEngine actual constructor(context: AndroidContext) {
	@ExtensionSdk
	@Suppress("UNCHECKED_CAST")
	actual suspend fun <T> evaluate(script: String) = withContext(Dispatchers.Default) {
		QuickJSContext.create().use { it.evaluate(script) } as T
	}
}