package com.mrboomdev.awery.extension.loaders.yomi

import com.mrboomdev.awery.android.EditTextPreference
import com.mrboomdev.awery.android.ListPreference
import com.mrboomdev.awery.android.MultiSelectListPreference
import com.mrboomdev.awery.android.SharedPreferences
import com.mrboomdev.awery.android.TwoStatePreference
import com.mrboomdev.awery.core.utils.notImplemented
import com.mrboomdev.awery.extension.sdk.BooleanPreference
import com.mrboomdev.awery.extension.sdk.IntPreference
import com.mrboomdev.awery.extension.sdk.LabelPreference
import com.mrboomdev.awery.extension.sdk.LongPreference
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.PreferenceGroup
import com.mrboomdev.awery.extension.sdk.Preferences
import com.mrboomdev.awery.extension.sdk.SelectPreference
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.TriStatePreference
import com.russhwolf.settings.Settings
import java.util.Formatter

internal fun Preference<*>.save(store: Preferences, key: String) {
	when(this) {
		is SelectPreference -> store.putString(key, value)
		is BooleanPreference -> store.putBoolean(key, value)
		is StringPreference -> store.putString(key, value)
		is IntPreference -> store.putInt(key, value)
		
		// androidx.preference doesn't expose such types
		is LongPreference,
		is TriStatePreference -> throw UnsupportedOperationException()
		
		// No mutable state is being retained here
		is LabelPreference -> {}

		// It is an equivalent of the Set<String>
		is PreferenceGroup -> store.putStringSet(
			key = key, 
			value = items.filter { 
				it.value == true 
			}.map { it.key }.toSet()
		)
	}
}

internal fun com.mrboomdev.awery.android.Preference.toAweryPreference(
	prefix: String
): Preference<*>? {
	if(!isVisible()) return null

	return when(this) {
		is ListPreference -> SelectPreference(
			key = prefix + getKey(),
			name = getTitle()?.toString() ?: getKey()!!,
			description = getSummary()?.toString(),
			value = getValue()!!,
			values = getEntries()!!.mapIndexed { index, item ->
				SelectPreference.Item(
					key = getEntryValues()!![index].toString(),
					name = item.toString()
				)
			}
		)
		
		is MultiSelectListPreference -> PreferenceGroup(
			key = prefix + getKey(),
			name = getTitle()?.toString() ?: getKey()!!,
			description = getSummary()?.toString(),
			items = getEntries()!!.mapIndexed { index, item ->
				val value = getEntryValues()!![index]
				
				BooleanPreference(
					key = value.toString(),
					name = item.toString(),
					value = getValues()!!.contains(value)
				)
			}
		)
		
		is EditTextPreference -> StringPreference(
			key = prefix + getKey(),
			name = getTitle()?.toString() ?: getKey()!!,
			description = getSummary()?.toString(),
			value = getText() ?: ""
		)
		
		is TwoStatePreference -> BooleanPreference(
			key = prefix + getKey(),
			name = getTitle()?.toString() ?: getKey()!!,
			description = getSummary()?.toString(),
			value = isChecked()
		)
		
		else -> LabelPreference(
			key = prefix + getKey(),
			description = getSummary()?.toString(),
			name = (getTitle()?.toString() ?: getKey()) + " - ${this::class.qualifiedName} isn't supported yet!"
		)
	}
}