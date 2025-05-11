package com.mrboomdev.awery.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.util.Selection
import com.mrboomdev.awery.util.ui.dialog.SelectionDialog
import java.util.Locale

// TODO: Move all this shit to the :shared module
object AweryLocales {
	
	fun i18nCountryName(input: String): String {
		return when(input.lowercase(Locale.ROOT)) {
			"us", "usa" -> Locale.US
			"china", "cn", "ch", "chinese", "zh" -> Locale.CHINA
			"ja", "jp", "japan", "jpn", "jap" -> Locale.JAPAN
			"ru", "russia", "rus" -> Locale.forLanguageTag("ru")
			"ko", "korea", "kor", "kr" -> Locale.KOREA
			else -> Locale.forLanguageTag(input)
		}.let { locale ->
			locale.displayCountry.replaceFirstChar { it.uppercase(locale) }
		}
	}

	fun i18nLanguageName(input: String): String {
		return when(input.lowercase(Locale.ROOT)) {
			"en", "us-US", "eng", "english" -> Locale.ENGLISH
			"zh", "chs", "chinese" -> Locale.CHINESE
			"ja", "jp", "japanese" -> Locale.JAPANESE
			"ru", "rus", "russian" -> Locale.forLanguageTag("ru")
			"ko", "kor", "korean" -> Locale.KOREAN
			else -> Locale.forLanguageTag(input)
		}.let { locale ->
			locale.displayLanguage.replaceFirstChar { it.uppercase(locale) }
		}
	}

	fun showPicker(context: Context) = showCustomPicker(context)

	private fun showCustomPicker(context: Context) {
		val currentLocales = LocaleManagerCompat.getApplicationLocales(context).let {
			if(it.size() == 0) {
				return@let LocaleManagerCompat.getSystemLocales(context)
			}

			return@let it
		}.toList()

		val availableLocales = availableLocales.sortedBy { it.second }

		val currentLocale = currentLocales.find { currentLocale ->
			availableLocales.containsLocale(currentLocale)
		}

		SelectionDialog.single(context, Selection(availableLocales.map {
			Selection.Selectable(it.first, it.first.toString(), it.second,
				if(it.first.areMostlySame(currentLocale)) Selection.State.SELECTED else Selection.State.UNSELECTED)
		})).setTitle(i18n(Res.string.select_language))
			.setPositiveButton(i18n(Res.string.save)) { dialog, selection ->
				selection.get(Selection.State.SELECTED)?.let {
					AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(it.item))
					Platform.restartApp()
				}

				dialog.dismiss()
			}
			.setNegativeButton(i18n(Res.string.cancel)) { it.dismiss() }
			.show()
	}

	
	private val availableLocales: List<Pair<Locale, String>>
		get() = mutableListOf<MutablePair<Locale, String>>().apply {
			addAll(AweryConfigurations.locales.map {
				MutablePair(Locale.forLanguageTag(it), i18nLanguageName(it))
			})

			for(locale in this) {
				for(locale2 in this) {
					if(locale == locale2) continue

					if(locale.originalSecond == locale2.originalSecond) {
						val country = locale2.first.displayCountry
						locale2.second = "${locale2.originalSecond} ($country)"
					}
				}
			}
		}.map {
			it.first to it.second
		}

	private fun LocaleListCompat.toList(): List<Locale> {
		return mutableListOf<Locale>().also { list ->
			for(i in 0 until size()) {
				list.add(get(i)!!)
			}
		}
	}

	private fun List<Pair<Locale, String>>.containsLocale(locale: Locale): Boolean {
		for((comparedLocale) in this) {
			if(locale.language == comparedLocale.language) {
				if(locale.areMostlySame(comparedLocale)) {
					return true
				}
			}
		}

		val filtered = filter { it.first.language == locale.language }
		return filtered.size == 1
	}

	private fun Locale.areMostlySame(other: Locale?): Boolean {
		if(other == null) {
			return false
		}

		if(language != other.language) {
			return false
		}

		if(country == "" || other.country == "" || country == other.country) {
			return true
		}

		return false
	}

	private class MutablePair<F, S>(var first: F, var second: S) {
		val originalSecond = second
	}
}