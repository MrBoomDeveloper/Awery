package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.Context
import android.content.pm.PackageInfo
import com.mrboomdev.awery.R
import com.mrboomdev.awery.ext.AndroidImage
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.sources.yomi.YomiManager
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory
import eu.kanade.tachiyomi.source.SourceFactory

class AniyomiManager(
	context: Context
): YomiManager<AnimeSource>(
	androidContext = context,
	id = ID,
	name = "Aniyomi",
	icon = AndroidImage(R.drawable.logo_aniyomi)
) {
	override val minVersion = 12.0
	override val maxVersion = 15.0

	override val appLabelPrefix = "Aniyomi: "
	override val nsfwMeta = "tachiyomi.animeextension.nsfw"
	override val mainClass = "tachiyomi.animeextension.class"
	override val requiredFeature = "tachiyomi.animeextension"

	private fun getSelected(it: Any?): AnimeSource? {
		if(it == null) {
			return null
		}

		if(it is AnimeSource) {
			return it
		}

		if(it is AnimeSourceFactory) {
			return getSelected(it.createSources())
		}

		if(it is List<*>) {
			return getSelected(it.toTypedArray())
		}

		if(it is Array<*>) {
			for(i in it) {
				if(i is AnimeSourceFactory) {
					return getSelected(i.createSources())
				}

				if(i is AnimeSource) {
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
		AniyomiSource(
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

	override fun getSourceLongId(source: AnimeSource): Long {
		return source.id
	}

	companion object {
		const val ID = "ANIYOMI_KOTLIN"
	}
}