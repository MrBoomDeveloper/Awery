package com.mrboomdev.awery.sources.yomi.aniyomi

import android.content.Context
import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Image
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.YomiSource
import com.mrboomdev.awery.util.AndroidImage
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory

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

	@Throws(IllegalArgumentException::class)
	override fun createSourceWrapper(
		label: String,
		isNsfw: Boolean,
		packageInfo: PackageInfo,
		source: Any?,
		exception: Throwable?
	): AniyomiSource {
		val selectedSource = source?.let {
			if(it is AnimeSource) {
				return@let it
			}

			if(it is Array<*> && it.isArrayOf<AnimeSource>()) {
				return@let getSelectedSource(it.filterIsInstance<AnimeSource>())
			}

			if(it is AnimeSourceFactory) {
				return@let getSelectedSource(it.createSources())
			}

			throw IllegalArgumentException("This is not an AnimeSource!")
		}

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