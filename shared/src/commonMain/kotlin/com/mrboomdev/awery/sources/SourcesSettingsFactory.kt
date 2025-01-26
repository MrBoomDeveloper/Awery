package com.mrboomdev.awery.sources

import com.mrboomdev.awery.data.settings.InMemorySetting
import com.mrboomdev.awery.data.settings.InMemorySettingsFactory

class SourcesSettingsFactory: InMemorySettingsFactory {
	override fun create(setting: InMemorySetting): InMemorySetting {
		throw UnsupportedOperationException("Sync setting creation isn't implemented!")
	}
}