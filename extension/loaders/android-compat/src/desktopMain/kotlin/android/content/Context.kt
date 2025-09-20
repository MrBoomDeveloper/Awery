package android.content

import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
abstract class Context {
    companion object {
        @JvmStatic
        @PlatformSdk
        val MODE_PRIVATE = 0
    }
}