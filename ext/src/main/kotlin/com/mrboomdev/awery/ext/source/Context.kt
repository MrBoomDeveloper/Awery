package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.util.Image

interface Context {
	val features: Array<AweryFeature>
	val id: String
	val isEnabled: Boolean
	val name: String?
	val exception: Throwable?
	val icon: Image?

	interface SourceContext: Context {
		val manager: SourcesManager
		val ageRating: AgeRating?
	}
}