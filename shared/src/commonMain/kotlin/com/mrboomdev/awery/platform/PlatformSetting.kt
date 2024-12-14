package com.mrboomdev.awery.platform

import com.mrboomdev.awery.app.PlatformPreferences
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.utils.parseEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.lang.Boolean.parseBoolean
import java.lang.Float.parseFloat
import java.lang.Integer.parseInt

@Serializable
class PlatformSetting(
	@SerialName("is_lazy")
	val isLazy: Boolean = false,
	override val type: Type? = null,
	override val key: String? = null,
	override val title: String? = null,
	override val description: String? = null,
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
): Setting() {

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
			super.value = when(type) {
				Type.STRING, Type.SELECT -> PlatformPreferences.getString(key) ?: defaultValue as String?
				Type.FLOAT -> PlatformPreferences.getFloat(key) ?: defaultValue as Float?
				Type.INTEGER -> PlatformPreferences.getInt(key) ?: defaultValue as Int?
				Type.BOOLEAN, Type.SCREEN_BOOLEAN -> PlatformPreferences.getBoolean(key) ?: defaultValue as Boolean?

				Type.TRI_STATE -> PlatformPreferences.getString(key).let { saved ->
					saved?.parseEnum<TriState>()?.also { return@let it }
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
	override var value: Any? = null
		get() = super.value
		set(value) {
			field = value
			saveValue()
		}

	private fun saveValue() {
		if(key == null) return

		PlatformPreferences.apply {
			when(val value = value) {
				is Boolean -> set(key, value)
				is String -> set(key, value)
				is Int -> set(key, value)
				is Float -> set(key, value)
				is TriState -> set(key, value.name)
				null -> remove(key)
				else -> throw UnsupportedOperationException("Unsupported setting value type!")
			}
		}.save()
	}

	override fun onClick() {
		throw NotImplementedError("You have to handle an click by yourself!")
	}
}