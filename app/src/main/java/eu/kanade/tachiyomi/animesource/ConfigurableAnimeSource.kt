package eu.kanade.tachiyomi.animesource

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceScreen
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

interface ConfigurableAnimeSource : AnimeSource {

    fun getSourcePreferences(): SharedPreferences = Injekt.get<Application>().getSharedPreferences(preferenceKey(), 0)

    fun setupPreferenceScreen(screen: PreferenceScreen)
}

fun ConfigurableAnimeSource.preferenceKey(): String = "source_$id"

fun ConfigurableAnimeSource.sourcePreferences(): SharedPreferences = Injekt.get<Application>().getSharedPreferences(preferenceKey(), 0)