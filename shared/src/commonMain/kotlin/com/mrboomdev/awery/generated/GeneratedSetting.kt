package com.mrboomdev.awery.generated

import com.mrboomdev.awery.platform.PlatformPreferences
import com.mrboomdev.awery.ext.data.getRecursively
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.utils.parseEnum
import kotlin.reflect.KProperty

sealed class GeneratedSetting(
	val key: kotlin.String,
	private val parentMap: PlatformSetting
) {
	
	fun asPlatformSetting() = parentMap.items!!.getRecursively(key) as PlatformSetting
	
	class Action(
		key: kotlin.String,
		parentMap: PlatformSetting
	): GeneratedSetting(key, parentMap)
	
	class Screen(
		key: kotlin.String,
		val items: Array<GeneratedSetting>,
		parentMap: PlatformSetting
	): GeneratedSetting(key, parentMap)
	
	interface WithValue<T> {
		var value: T
		operator fun getValue(thisRef: Any?, property: KProperty<*>): T = this.value
		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
	}

	class Select<T: Enum<T>>(
		key: kotlin.String,
		val clazz: Class<T>,
		parentMap: PlatformSetting
	): GeneratedSetting(key, parentMap), WithValue<T?> {
		override var value: T?
			get() = PlatformPreferences.getString(key)?.parseEnum(clazz)
			set(value) = PlatformPreferences.let {
				it[key] = value?.name
				it.save()
			}
		
		class NotNull<T: Enum<T>>(
			key: kotlin.String,
			val clazz: Class<T>,
			private val defaultValue: T,
			parentMap: PlatformSetting
		): GeneratedSetting(key, parentMap), WithValue<T> {
			override var value: T
				get() = PlatformPreferences.getString(key)?.parseEnum(clazz) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value.name
					it.save()
				}
		}
	}

	class Integer(
		key: kotlin.String,
		parentMap: PlatformSetting
	): GeneratedSetting(key, parentMap), WithValue<Int?> {
		override var value: Int?
			get() = PlatformPreferences.getInt(key)
			set(value) = PlatformPreferences.let {
				it[key] = value
				it.save()
			}
		
		class NotNull(
			key: kotlin.String,
			private val defaultValue: Int,
			parentMap: PlatformSetting
		): GeneratedSetting(key, parentMap), WithValue<Int> {
			override var value: Int
				get() = PlatformPreferences.getInt(key) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value
					it.save()
				}
		}
	}

	class Boolean(
		key: kotlin.String,
		parentMap: PlatformSetting
	): GeneratedSetting(key, parentMap), WithValue<kotlin.Boolean?> {
		override var value: kotlin.Boolean?
			get() = PlatformPreferences.getBoolean(key)
			set(value) = PlatformPreferences.let {
				it[key] = value
				it.save()
			}
		
		class NotNull(
			key: kotlin.String,
			private val defaultValue: kotlin.Boolean,
			parentMap: PlatformSetting
		): GeneratedSetting(key, parentMap), WithValue<kotlin.Boolean> {
			override var value: kotlin.Boolean
				get() = PlatformPreferences.getBoolean(key) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value
					it.save()
				}
		}
	}

	class String(
		key: kotlin.String,
		parentMap: PlatformSetting
	): GeneratedSetting(key, parentMap), WithValue<kotlin.String?> {
		override var value: kotlin.String?
			get() = PlatformPreferences.getString(key)
			set(value) = PlatformPreferences.let {
				it[key] = value
				it.save()
			}
		
		class NotNull(
			key: kotlin.String,
			private val defaultValue: kotlin.String,
			parentMap: PlatformSetting
		): GeneratedSetting(key, parentMap), WithValue<kotlin.String> {
			override var value: kotlin.String
				get() = PlatformPreferences.getString(key) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value
					it.save()
				}
		}
	}
}