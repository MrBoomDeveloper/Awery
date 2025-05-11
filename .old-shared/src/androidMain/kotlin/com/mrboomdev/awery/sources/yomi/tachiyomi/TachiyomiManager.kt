package com.mrboomdev.awery.sources.yomi.tachiyomi

import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.util.Image
import com.mrboomdev.awery.sources.yomi.YomiManager
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class TachiyomiManager: YomiManager<Source>(
	id = ID,
	name = "Tachiyomi"
) {
	override val minVersion = 1.2
	override val maxVersion = 1.5

	override val appLabelPrefix = "Tachiyomi: "
	override val nsfwMeta = "tachiyomi.extension.nsfw"
	override val mainClass = "tachiyomi.extension.class"
	override val requiredFeature = "tachiyomi.extension"

	private fun getSelected(it: Any?): Source? {
		if(it == null) {
			return null
		}

		if(it is Source) {
			return it
		}

		if(it is SourceFactory) {
			return getSelected(it.createSources())
		}

		if(it is List<*>) {
			return getSelected(it.toTypedArray())
		}

		if(it is Array<*>) {
			for(i in it) {
				if(i is SourceFactory) {
					return getSelected(i.createSources())
				}

				if(i is Source) {
					// TODO: Return an source selected by the user.
					return i
				}
			}
		}

		throw UnsupportedOperationException("Unsupported source type!")
	}
	
	override fun createSourceWrapper(
		icon: Image,
		label: String,
		isNsfw: Boolean,
		packageInfo: PackageInfo,
		sources: Array<Any>?,
		exception: Throwable?
	) = getSelected(sources).let { selectedSource ->
		TachiyomiSource(
			isEnabled = selectedSource != null,
			source = selectedSource,
			packageInfo = packageInfo,
			manager = this,
			exception = exception,
			name = selectedSource?.name ?: label,
			icon = icon,
			ageRating = if(isNsfw) {
				AgeRating.NSFW
			} else null
		)
	}

	override fun getSourceLongId(source: Source): Long {
		return source.id
	}

	companion object {
		const val ID = "TACHIYOMI_KOTLIN"
	}
}