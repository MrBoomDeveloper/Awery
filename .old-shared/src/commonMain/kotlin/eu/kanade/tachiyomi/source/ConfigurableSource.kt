package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.PreferenceScreen
import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
interface ConfigurableSource : Source {
    @ExtensionSdk
    fun setupPreferenceScreen(screen: PreferenceScreen)
}