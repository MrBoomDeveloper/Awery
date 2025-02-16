package eu.kanade.tachiyomi.network

import com.mrboomdev.awery.AndroidContext
import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
expect class JavaScriptEngine(context: AndroidContext) {
	@ExtensionSdk
	suspend fun <T> evaluate(script: String): T
}