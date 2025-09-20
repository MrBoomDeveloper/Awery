package eu.kanade.tachiyomi.animesource

import com.mrboomdev.awery.android.AndroidUtils
import com.mrboomdev.awery.android.PreferenceScreen
import com.mrboomdev.awery.android.SharedPreferences
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
interface ConfigurableAnimeSource : AnimeSource {
    @PlatformSdk
    fun setupPreferenceScreen(screen: PreferenceScreen)
}