package com.mrboomdev.awery.utils

import com.mrboomdev.awery.ext.data.Setting
import kotlinx.serialization.Serializable

private class SettingSerializer: ReflectionSerializer<Setting>()

@Serializable
class SettingWrapper(
	val setting: @Serializable(SettingSerializer::class) Setting
)

/**
 * You now can use the power of the runtime-serializer selection!
 */
fun Setting.wrap() = SettingWrapper(this)