package com.mrboomdev.awery.data.settings

object Settings {
    val darkTheme by setting(DarkTheme.AUTO)
    val amoledTheme by setting(false)
    val devMode by setting(false)
    val checkAppUpdates by setting(true)
    val autoUpdateExtensions by setting(true)
    val externalPlayer by setting(true)
    val playbackDoubleTapRewind by setting(15)

    enum class DarkTheme {
        AUTO, ON, OFF
    }
}