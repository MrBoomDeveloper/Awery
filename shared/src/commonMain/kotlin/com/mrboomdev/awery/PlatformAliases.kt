package com.mrboomdev.awery

expect abstract class AndroidContext
expect interface SharedPreferences
expect class AndroidEditText
expect class Preference
expect class PreferenceScreen
expect class SwitchPreferenceCompat
expect abstract class TwoStatePreference
expect class CheckBoxPreference
expect abstract class DialogPreference
expect class EditTextPreference
expect class ListPreference
expect class MultiSelectListPreference

expect abstract class AndroidUri {
	abstract override fun toString(): String
}