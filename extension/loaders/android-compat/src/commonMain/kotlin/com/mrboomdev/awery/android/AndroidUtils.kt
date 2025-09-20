package com.mrboomdev.awery.android

expect object AndroidUtils {
    fun getSharedPreferences(name: String): SharedPreferences
    fun createPreferenceScreen(name: String): PreferenceScreen
}