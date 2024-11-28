package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.Context
import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.util.AndroidImage
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory
import eu.kanade.tachiyomi.source.SourceFactory

class AniyomiManager(
	context: Context
): YomiManager<AnimeSource, AniyomiSource>(context) {
	override val minVersion = 12.0
	override val maxVersion = 15.0
	override val name = "Aniyomi"
	override val id = ID

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
	): AniyomiSource {
		val selectedSource = getSelected(sources)

		return object : AniyomiSource(packageInfo, selectedSource) {
			override val manager = this@AniyomiManager
			override val exception = exception

			override val ageRating = (if(isNsfw) {
				AweryAgeRating.NSFW
			} else AweryAgeRating.EVERYONE).toString()

			override val icon = AndroidImage(packageInfo
				.applicationInfo!!.loadIcon(context.packageManager))

			override val name by lazy {
				selectedSource?.name ?: label
			}
		}
	}

	override fun getSourceLongId(source: AnimeSource): Long {
		return source.id
	}

	companion object {
		const val ID = "ANIYOMI_KOTLIN"
	}
}