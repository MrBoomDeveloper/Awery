package com.mrboomdev.awery.ui.popups

import java.util.Locale

internal actual fun getCurrentAppLocales(): List<Locale> {
    return listOf(Locale.getDefault())
}

internal actual fun setAppLocale(locale: Locale) {
    Locale.setDefault(locale)
}