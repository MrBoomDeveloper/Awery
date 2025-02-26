package com.mrboomdev.awery.ext.source.module

import com.mrboomdev.awery.ext.data.Setting

interface SettingsModule: Module {
	fun getSettings(): List<Setting>
}