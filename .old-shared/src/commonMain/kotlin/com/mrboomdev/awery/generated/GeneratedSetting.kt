package com.mrboomdev.awery.generated

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.mrboomdev.awery.platform.PlatformPreferences
import com.mrboomdev.awery.utils.annotations.AweryInternalApi
import com.mrboomdev.awery.utils.toEnum
import kotlin.reflect.KProperty

sealed class GeneratedSetting(val key: kotlin.String) {
	class Action(key: kotlin.String): GeneratedSetting(key)
	
	class Screen(
		key: kotlin.String,
		val items: Array<GeneratedSetting>
	): GeneratedSetting(key)
	
	interface WithValue<T> {
		@Suppress("FunctionName")
		@AweryInternalApi
		fun _setValue(value: Any?) {
			@Suppress("UNCHECKED_CAST")
			this.value = value as T
		}
		
		var value: T
		val state: State<T>
		operator fun getValue(thisRef: Any?, property: KProperty<*>): T = this.value
		operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) { this.value = value }
	}

	class Select<T: Enum<T>>(
		key: kotlin.String,
		val clazz: Class<T>
	): GeneratedSetting(key), WithValue<T?> {
		override var value: T?
			get() = PlatformPreferences.getString(key)?.toEnum(clazz)
			set(value) = PlatformPreferences.let {
				it[key] = value?.name
				it.save()
				_state.value = value
			}
		
		@AweryInternalApi
		override fun _setValue(value: Any?) {
			if(value == null) {
				this.value = null
			} else if(value is kotlin.String) {
				@Suppress("UNCHECKED_CAST")
				this.value = value.toEnum(clazz.javaClass as Class<T>)
			} else {
				throw UnsupportedOperationException("Unsupported value type! ${value::class.qualifiedName}")
			}
		}
		
		private val _state = mutableStateOf(value)
		override val state: State<T?> = _state
		
		class NotNull<T: Enum<T>>(
			key: kotlin.String,
			val clazz: Class<T>,
			private val defaultValue: T
		): GeneratedSetting(key), WithValue<T> {
			override var value: T
				get() = PlatformPreferences.getString(key)?.toEnum(clazz) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value.name
					it.save()
					_state.value = value
				}
			
			@AweryInternalApi
			override fun _setValue(value: Any?) {
				if(value == null) {
					throw UnsupportedOperationException("Null values aren't supported for this setting!")
				} else if(value is kotlin.String) {
					this.value = value.toEnum(clazz)!!
				} else {
					throw UnsupportedOperationException("Unsupported value type! ${value::class.qualifiedName}")
				}
			}
			
			private val _state = mutableStateOf(value)
			override val state: State<T> = _state
		}
	}

	class Integer(key: kotlin.String): GeneratedSetting(key), WithValue<Int?> {
		override var value: Int?
			get() = PlatformPreferences.getInt(key)
			set(value) = PlatformPreferences.let {
				it[key] = value
				it.save()
				_state.value = value
			}
		
		private val _state = mutableStateOf(value)
		override val state: State<Int?> = _state
		
		class NotNull(
			key: kotlin.String,
			private val defaultValue: Int
		): GeneratedSetting(key), WithValue<Int> {
			override var value: Int
				get() = PlatformPreferences.getInt(key) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value
					it.save()
					_state.value = value
				}
			
			private val _state = mutableStateOf(value)
			override val state: State<Int> = _state
		}
	}

	class Boolean(key: kotlin.String): GeneratedSetting(key), WithValue<kotlin.Boolean?> {
		override var value: kotlin.Boolean?
			get() = PlatformPreferences.getBoolean(key)
			set(value) = PlatformPreferences.let {
				it[key] = value
				it.save()
				_state.value = value
			}
		
		private val _state = mutableStateOf(value)
		override val state: State<kotlin.Boolean?> = _state
		
		class NotNull(
			key: kotlin.String,
			private val defaultValue: kotlin.Boolean
		): GeneratedSetting(key), WithValue<kotlin.Boolean> {
			override var value: kotlin.Boolean
				get() = PlatformPreferences.getBoolean(key) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value
					it.save()
					_state.value = value
				}
			
			private val _state = mutableStateOf(value)
			override val state: State<kotlin.Boolean> = _state
		}
	}

	class String(key: kotlin.String): GeneratedSetting(key), WithValue<kotlin.String?> {
		override var value: kotlin.String?
			get() = PlatformPreferences.getString(key)
			set(value) = PlatformPreferences.let {
				it[key] = value
				it.save()
				_state.value = value
			}
		
		private val _state = mutableStateOf(value)
		override val state: State<kotlin.String?> = _state
		
		class NotNull(
			key: kotlin.String,
			private val defaultValue: kotlin.String
		): GeneratedSetting(key), WithValue<kotlin.String> {
			override var value: kotlin.String
				get() = PlatformPreferences.getString(key) ?: defaultValue
				set(value) = PlatformPreferences.let {
					it[key] = value
					it.save()
					_state.value = value
				}
			
			private val _state = mutableStateOf(value)
			override val state: State<kotlin.String> = _state
		}
	}
}