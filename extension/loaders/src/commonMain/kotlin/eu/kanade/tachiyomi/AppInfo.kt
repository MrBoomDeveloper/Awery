package eu.kanade.tachiyomi

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.PlatformSdk
import com.mrboomdev.awery.data.appVersion
import com.mrboomdev.awery.data.appVersionCode

@PlatformSdk
object AppInfo {
    @PlatformSdk
    fun getVersionCode(): Int = Awery.appVersionCode
    
    @PlatformSdk
    fun getVersionName(): String = Awery.appVersion
}