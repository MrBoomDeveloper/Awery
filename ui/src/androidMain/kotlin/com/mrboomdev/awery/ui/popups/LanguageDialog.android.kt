package com.mrboomdev.awery.ui.popups

import android.app.LocaleManager
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.LocaleManagerCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.getSystemService
import androidx.core.os.LocaleListCompat
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import java.util.Locale
import kotlin.system.exitProcess

internal actual fun setAppLocale(locale: Locale) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
}

internal actual fun getCurrentAppLocales(): List<Locale> {
    return LocaleManagerCompat.getApplicationLocales(Awery.context).let {
        if(it.size() == 0) {
            return@let LocaleManagerCompat.getSystemLocales(Awery.context)
        }

        return@let it
    }.toList()
}

private fun LocaleListCompat.toList(): List<Locale> {
    return mutableListOf<Locale>().also { list ->
        for(i in 0 until size()) {
            list.add(get(i)!!)
        }
    }
}