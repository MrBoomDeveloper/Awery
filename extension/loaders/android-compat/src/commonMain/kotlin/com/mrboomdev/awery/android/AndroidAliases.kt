package com.mrboomdev.awery.android

expect interface SharedPreferences
expect abstract class Context

expect abstract class Uri {
    abstract override fun toString(): String
}

expect class PreferenceScreen {
    fun getPreferenceCount(): Int
    fun getPreference(index: Int): Preference
}

fun PreferenceScreen.getItems(): List<Preference> {
    return (0 until getPreferenceCount()).map { index ->
        getPreference(index)
    }
}

expect class EditTextPreference: Preference {
    fun getText(): String?
}

expect class ListPreference: Preference {
    fun getEntries(): Array<CharSequence>?
    fun getEntryValues(): Array<CharSequence>?
    fun getValue(): String?
}

expect class MultiSelectListPreference: Preference {
    fun getEntries(): Array<CharSequence>?
    fun getEntryValues(): Array<CharSequence>?
    fun getValues(): Set<String>?
}

expect abstract class TwoStatePreference: Preference {
    fun isChecked(): Boolean
}

expect open class Preference {
    fun getKey(): String?
    fun getTitle(): CharSequence?
    fun getSummary(): CharSequence?
    fun isVisible(): Boolean
}