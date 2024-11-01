package com.mrboomdev.awery.sources.yomi.tachiyomi

import android.content.Context
import android.content.pm.PackageInfo
import com.mrboomdev.awery.ext.constants.AweryAgeRating
import com.mrboomdev.awery.ext.source.Source
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.sources.yomi.YomiManager
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiManager.Companion
import com.mrboomdev.awery.sources.yomi.aniyomi.AniyomiSource
import com.mrboomdev.awery.util.AndroidImage
import eu.kanade.tachiyomi.animesource.AnimeSource
import eu.kanade.tachiyomi.animesource.AnimeSourceFactory
import eu.kanade.tachiyomi.source.MangaSource
import eu.kanade.tachiyomi.source.SourceFactory

class TachiyomiManager(
	context: Context
): YomiManager<MangaSource, TachiyomiSource>(context) {
	override val minVersion = 1.2
	override val maxVersion = 1.5
	override val name = "Tachiyomi"
	override val id = ID

	override val appLabelPrefix = "Tachiyomi: "
	override val nsfwMeta = "tachiyomi.extension.nsfw"
	override val mainClass = "tachiyomi.extension.class"
	override val requiredFeature = "tachiyomi.extension"

	@Throws(IllegalArgumentException::class)
	override fun createSourceWrapper(
		label: String,
		isNsfw: Boolean,
		packageInfo: PackageInfo,
		source: Any?,
		exception: Throwable?
	): TachiyomiSource {
		val selectedSource = source?.let {
			if(it is MangaSource) {
				return@let it
			}

			if(it is Array<*> && it.isArrayOf<MangaSource>()) {
				return@let getSelectedSource(it.filterIsInstance<MangaSource>())
			}

			if(it is SourceFactory) {
				return@let getSelectedSource(it.createSources())
			}

			throw IllegalArgumentException("This is not an MangaSource!")
		}

		return object : TachiyomiSource(packageInfo, selectedSource) {
			override val manager = this@TachiyomiManager
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

	override fun getSourceLongId(source: MangaSource): Long {
		return source.id
	}

	companion object {
		const val ID = "TACHIYOMI_KOTLIN"
	}
}