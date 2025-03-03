package com.mrboomdev.awery.data.settings

import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ext.util.Image
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.PlatformPreferences
import com.mrboomdev.awery.platform.areRequirementsMet
import com.mrboomdev.awery.utils.annotations.AweryInternalApi
import com.mrboomdev.awery.utils.toEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.lang.Boolean.parseBoolean
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt

/**
 * This setting is from an trusted source,
 * so it have capabilities like restart suggestions and platform value reading.
 */
@Serializable
class PlatformSetting(
	@SerialName("lazy_factory")
	val lazyFactory: InMemorySettingsFactoryClass? = null,
	@SerialName("lazy_suspend")
	val lazySuspend: String? = null,
	override val type: Type? = null,
	override val key: String? = null,
	override val title: String? = null,
	override val description: String? = null,
	val placeholder: String? = null,
	override val items: List<PlatformSetting>? = null,
	@Suppress("PrivatePropertyName") @SerialName("value")
	private val __defaultValue: String? = null,
	val restart: Boolean = false,
	@SerialName("show_if")
	val showIf: Array<String>? = null,
	@SerialName("icon")
	val iconRes: String? = null,
	override val from: Float? = null,
	override val to: Float? = null,
	@SerialName("icon_scale")
	val iconScale: Float? = null
): Setting(), java.io.Serializable {
	@Transient
	override val icon: Image? = null
	
	@Transient
	val defaultValue = __defaultValue?.let {
		when(type) {
			Type.STRING, Type.SELECT -> it
			Type.TRI_STATE -> TriState.valueOf(it)
			Type.FLOAT -> parseFloat(it)
			Type.INTEGER -> parseInt(it)
			Type.BOOLEAN, Type.SCREEN_BOOLEAN -> parseBoolean(it)
			else -> null
		}
	}

	fun restoreValues() {
		if(key != null) {
			_value = when(type) {
				Type.STRING, Type.SELECT -> PlatformPreferences.getString(key) ?: defaultValue as String?
				Type.FLOAT -> PlatformPreferences.getFloat(key) ?: defaultValue as Float?
				Type.INTEGER -> PlatformPreferences.getInt(key) ?: defaultValue as Int?
				Type.BOOLEAN, Type.SCREEN_BOOLEAN -> PlatformPreferences.getBoolean(key) ?: defaultValue as Boolean?

				Type.TRI_STATE -> PlatformPreferences.getString(key).let { saved ->
					saved?.toEnum<TriState>()?.also { return@let it }
					defaultValue as TriState?
				}

				else -> null
			}
		}

		items?.let {
			for(item in it) {
				item.restoreValues()
			}
		}
	}

	override val isVisible: Boolean
		get() = showIf?.let { areRequirementsMet(it) } ?: true
	
	@Transient
	private var _value: Any? = null

	@OptIn(AweryInternalApi::class)
	override var value: Any?
		get() = _value
		set(value) {
			_value = value
			
			(AwerySettings.all[key] as? GeneratedSetting.WithValue<*>)?.also {
				it._setValue(value)
			} ?: run {
				saveValue(value)
			}
		}

	private fun saveValue(newValue: Any?) {
		requireNotNull(key)
		
		PlatformPreferences.apply {
			when(newValue) {
				is Boolean -> set(key, newValue)
				is String -> set(key, newValue)
				is Int -> set(key, newValue)
				is Float -> set(key, newValue)
				is TriState -> set(key, newValue.name)
				null -> remove(key)
				else -> throw UnsupportedOperationException("Unsupported setting value type!")
			}
		}
		
		PlatformPreferences.save()
	}

	override fun onClick() {
		throw NotImplementedError("You have to handle an click by yourself!")
	}
}