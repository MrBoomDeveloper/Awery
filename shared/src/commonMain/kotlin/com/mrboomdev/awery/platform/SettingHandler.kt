package com.mrboomdev.awery.platform

import androidx.compose.runtime.compositionLocalOf
import com.mrboomdev.awery.ext.data.Setting

interface SettingHandler {
	fun openScreen(screen: Setting)
	fun handleClick(setting: Setting)
}

val LocalSettingHandler = compositionLocalOf<SettingHandler> { 
	throw NotImplementedError("No LocalSettingHandler was provided!")
}