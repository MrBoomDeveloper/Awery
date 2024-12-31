package com.mrboomdev.awery.sources.yomi.tachiyomi

import android.content.Context
import android.content.pm.PackageInfo
import com.mrboomdev.awery.R
import com.mrboomdev.awery.ext.AndroidImage
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.sources.yomi.YomiManager
import eu.kanade.tachiyomi.source.MangaSource
import eu.kanade.tachiyomi.source.SourceFactory

class TachiyomiManager(
	context: Context
): YomiManager<MangaSource>(
	androidContext = context,
	id = ID,
	name = "Tachiyomi",
	icon = AndroidImage(R.drawable.logo_tachiyomi)
) {
	override val minVersion = 1.2
	override val maxVersion = 1.5

	override val appLabelPrefix = "Tachiyomi: "
	override val nsfwMeta = "tachiyomi.extension.nsfw"
	override val mainClass = "tachiyomi.extension.class"
	override val requiredFeature = "tachiyomi.extension"

	private fun getSelected(it: Any?): MangaSource? {
		if(it == null) {
			return null
		}

		if(it is MangaSource) {
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

				if(i is MangaSource) {
					// TODO: Return an source selected by the user.
					return i
				}
			}
		}

		throw UnsupportedOperationException("Unsupported source type!")
	}

	@Throws(IllegalArgumentException::class)
	override fun createSourceWrapper(
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

			icon = AndroidImage(packageInfo.applicationInfo!!
				.loadIcon(androidContext.packageManager)),

			ageRating = if(isNsfw) {
				AweryAgeRating.NSFW
			} else null
		)
	}

	override fun getSourceLongId(source: MangaSource): Long {
		return source.id
	}

	companion object {
		const val ID = "TACHIYOMI_KOTLIN"
	}
}