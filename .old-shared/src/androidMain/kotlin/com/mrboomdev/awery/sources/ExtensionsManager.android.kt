package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.tachiyomi.TachiyomiManager

internal actual fun ExtensionsManager.BootstrapManager.createPlatformSourceManagers(): List<SourcesManager> {
	YomiManager.initYomiShit()
	
	return listOf(
		AniyomiManager(),
		TachiyomiManager()
	)
}