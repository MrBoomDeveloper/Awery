package com.mrboomdev.awery.sources

import com.mrboomdev.awery.ext.source.SourcesManager

internal actual fun ExtensionsManager.BootstrapManager.createPlatformSourceManagers(): List<SourcesManager> {
	return emptyList()
}