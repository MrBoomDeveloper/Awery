package eu.kanade.tachiyomi

import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
object AppInfo {

	@ExtensionSdk
	fun getVersionCode(): Int = BuildKonfig.VERSION_CODE
	

	@ExtensionSdk
	fun getVersionName(): String = BuildKonfig.VERSION_NAME
}