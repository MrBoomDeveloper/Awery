package com.mrboomdev.awery.ext.source

import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.constants.AweryFeature
import com.mrboomdev.awery.ext.util.Image

abstract class Context {
	abstract val features: Array<AweryFeature>
	abstract val id: String
	abstract val isEnabled: Boolean
	abstract val name: String?
	abstract val exception: Throwable?
	abstract val icon: Image?

	abstract class SourceContext: Context() {
		abstract val manager: SourcesManager
		abstract val ageRating: AweryAgeRating?
	}
}