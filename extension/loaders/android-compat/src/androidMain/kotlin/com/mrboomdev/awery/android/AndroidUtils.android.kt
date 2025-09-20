package com.mrboomdev.awery.android

import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceManager
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import java.io.File

actual object AndroidUtils {
    actual fun getSharedPreferences(name: String): SharedPreferences {
        return Awery.context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }
    
    actual fun createPreferenceScreen(name: String): PreferenceScreen {
        return PreferenceManager(Awery.context).apply {
            sharedPreferencesName = name
            setPreferences(createPreferenceScreen(context))
        }.preferenceScreen
    }
}