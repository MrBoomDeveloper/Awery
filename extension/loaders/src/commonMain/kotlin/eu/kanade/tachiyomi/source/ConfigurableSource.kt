package eu.kanade.tachiyomi.source

import com.mrboomdev.awery.android.AndroidUtils
import com.mrboomdev.awery.android.SharedPreferences
import com.mrboomdev.awery.android.PreferenceScreen
import com.mrboomdev.awery.core.utils.PlatformSdk

@PlatformSdk
interface ConfigurableSource : MangaSource {

    /**
     * Gets instance of [SharedPreferences] scoped to the specific source.
     *
     * @since extensions-lib 1.5
     */
    fun getSourcePreferences(): SharedPreferences =
        AndroidUtils.getSharedPreferences(preferenceKey())

    @PlatformSdk
    fun setupPreferenceScreen(screen: PreferenceScreen)
}

fun ConfigurableSource.preferenceKey(): String = "source_$id"

// TODO: use getSourcePreferences once all extensions are on ext-lib 1.5
fun ConfigurableSource.sourcePreferences(): SharedPreferences =
    AndroidUtils.getSharedPreferences(preferenceKey())

fun sourcePreferences(key: String): SharedPreferences =
    AndroidUtils.getSharedPreferences(key)