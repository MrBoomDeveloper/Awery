package com.mrboomdev.awery

import com.mrboomdev.awery.app.PlatformPreferences
import com.mrboomdev.awery.utils.parseEnum
import kotlin.reflect.KProperty

sealed class GeneratedSetting(val key: kotlin.String) {
	class Action(
		key: kotlin.String
	): GeneratedSetting(key)

	class Select<T: Enum<T>>(
		key: kotlin.String,
		val clazz: Class<T>
	): GeneratedSetting(key) {
		var value: T?
			get() = PlatformPreferences.getString(key)?.parseEnum(clazz)
			set(value) {
				if(value != null) {
					PlatformPreferences[key] = value.name
				} else {
					PlatformPreferences.remove(key)
				}

				PlatformPreferences.save()
			}

		operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = this.value
		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) { this.value = value }
	}

	class Screen(
		key: kotlin.String,
		val items: Array<GeneratedSetting>
	): GeneratedSetting(key)

	class Integer(
		key: kotlin.String
	): GeneratedSetting(key) {
		var value: Int?
			get() = PlatformPreferences.getInt(key)
			set(value) {
				if(value != null) {
					PlatformPreferences[key] = value
				} else {
					PlatformPreferences.remove(key)
				}

				PlatformPreferences.save()
			}

		operator fun getValue(thisRef: Any?, property: KProperty<*>): Int? = this.value
		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) { this.value = value }
	}

	class Boolean(
		key: kotlin.String
	): GeneratedSetting(key) {
		var value: kotlin.Boolean?
			get() = PlatformPreferences.getBoolean(key)
			set(value) {
				if(value != null) {
					PlatformPreferences[key] = value
				} else {
					PlatformPreferences.remove(key)
				}

				PlatformPreferences.save()
			}

		operator fun getValue(thisRef: Any?, property: KProperty<*>): kotlin.Boolean? = this.value
		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: kotlin.Boolean?) { this.value = value }
	}

	class String(
		key: kotlin.String
	): GeneratedSetting(key) {
		var value: kotlin.String?
			get() = PlatformPreferences.getString(key)
			set(value) {
				if(value != null) {
					PlatformPreferences[key] = value
				} else {
					PlatformPreferences.remove(key)
				}

				PlatformPreferences.save()
			}

		operator fun getValue(thisRef: Any?, property: KProperty<*>): kotlin.String? = this.value
		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: kotlin.String?) { this.value = value }
	}
}