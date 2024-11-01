package com.mrboomdev.awery.app

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.LocaleManagerCompat
import androidx.core.os.LocaleListCompat
import com.mrboomdev.awery.R
import com.mrboomdev.awery.util.Selection
import com.mrboomdev.awery.util.extensions.getResourceId
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.ui.dialog.SelectionDialog
import org.xmlpull.v1.XmlPullParser
import java.util.Locale

object AweryLocales {
	private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

	fun translateCountryName(context: Context, input: String): String {
		return when(input.lowercase(Locale.ROOT)) {
			"us", "usa" -> context.getString(R.string.us)
			"china", "cn", "ch", "chinese", "zh" -> context.getString(R.string.china)
			"ja", "jp", "japan", "jpn", "jap" -> context.getString(R.string.japan)
			"ru", "russia", "rus" -> context.getString(R.string.russia)
			"ko", "korea", "kor", "kr" -> context.getString(R.string.korea)
			else -> Locale.forLanguageTag(input).displayCountry
		}
	}

	fun translateLangName(context: Context, input: String): String {
		return when(input.lowercase(Locale.ROOT)) {
			"en", "us-US", "eng", "english" -> context.getString(R.string.english)
			"zh", "chs", "chinese" -> context.getString(R.string.chinese)
			"ja", "jp", "japanese" -> context.getString(R.string.japanese)
			"ru", "rus", "russian" -> context.getString(R.string.russian)
			"ko", "kor", "korean" -> context.getString(R.string.korean)
			else -> Locale.forLanguageTag(input).displayLanguage
		}
	}

	fun showPicker(context: Context) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			context.startActivity(action = Settings.ACTION_APP_LOCALE_SETTINGS,
				data = Uri.parse("package:${context.packageName}"))
		} else {
			showCustomPicker(context)
		}
	}

	private fun showCustomPicker(context: Context) {
		val currentLocales = LocaleManagerCompat.getApplicationLocales(context).let {
			if(it.size() == 0) {
				return@let LocaleManagerCompat.getSystemLocales(context)
			}

			return@let it
		}.toList()

		val availableLocales = getAvailableLocales(context).sortedBy { it.second }

		val currentLocale = currentLocales.find { currentLocale ->
			availableLocales.containsLocale(currentLocale)
		}

		SelectionDialog.single(context, Selection(availableLocales.map {
			Selection.Selectable(it.first, it.first.toString(), it.second,
				if(it.first.areMostlySame(currentLocale)) Selection.State.SELECTED else Selection.State.UNSELECTED)
		})).setTitle("Select language")
			.setPositiveButton(R.string.save) { dialog, selection ->
				selection.get(Selection.State.SELECTED)?.let {
					AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(it.item))
				}

				dialog.dismiss()
			}
			.setNegativeButton(R.string.cancel) { it.dismiss() }
			.show()
	}

	private fun getAvailableLocales(context: Context): List<Pair<Locale, String>> {
		return mutableListOf<MutablePair<Locale, String>>().apply {
			// All locales are being stored in this little file.
			// It is being auto-generated, so we need to use some reflection.
			val fileId = getResourceId<R.xml>("_generated_res_locale_config")

			context.resources.getXml(fileId).use {
				while(it.eventType != XmlPullParser.END_DOCUMENT) {
					when(it.eventType) {
						XmlPullParser.START_DOCUMENT -> {}

						XmlPullParser.START_TAG -> {
							if(it.name == "locale") {
								val value = it.getAttributeValue(ANDROID_NAMESPACE, "name")
								add(MutablePair(Locale.forLanguageTag(value), translateLangName(context, value)))
							}
						}
					}

					it.next()
				}
			}

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