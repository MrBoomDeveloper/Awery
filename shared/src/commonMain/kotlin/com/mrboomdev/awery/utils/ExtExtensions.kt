package com.mrboomdev.awery.utils

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogMedia.Status
import com.mrboomdev.awery.ext.data.CatalogMedia.Type
import com.mrboomdev.awery.ext.data.CatalogTag
import com.mrboomdev.awery.generated.*
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

val CatalogMedia.bannerOrPoster 
	get() = extras[CatalogMedia.EXTRA_BANNER] ?: extras[CatalogMedia.EXTRA_POSTER]

val CatalogMedia.posterOrBanner 
	get() = extras[CatalogMedia.EXTRA_POSTER] ?: extras[CatalogMedia.EXTRA_BANNER]

val CatalogMedia.status: Status?
	get() = extras[CatalogMedia.EXTRA_STATUS]?.uppercase()?.let {
		try { Status.valueOf(it) } catch(_: Throwable) { null }
	}

@get:Composable
val CatalogMedia.statusLocalized: String?
	get() = status?.let { 
		when(it) {
			Status.ONGOING -> stringResource(Res.string.status_releasing)
			Status.COMPLETED -> stringResource(Res.string.status_finished)
			Status.COMING_SOON -> stringResource(Res.string.status_not_yet_released)
			Status.PAUSED -> stringResource(Res.string.status_hiatus)
			Status.CANCELLED -> stringResource(Res.string.status_cancelled)
		}
	} ?: extras[CatalogMedia.EXTRA_STATUS]

@get:Composable
val CatalogMedia.ageRatingLocalized: String?
	get() = ageRating?.let { 
		"${it.age}+"
	} ?: extras[CatalogMedia.EXTRA_AGE_RATING]

val CatalogMedia.type: Type?
	get() = when(extras[CatalogMedia.EXTRA_TYPE]?.lowercase()) {
		"movie", "tv" -> Type.MOVIE
		"book", "comic" -> Type.BOOK
		"post" -> Type.POST
		"video" -> Type.VIDEO
		"music" -> Type.MUSIC
		else -> null
	}

val CatalogMedia.authors: Array<String>?
	get() = extras[CatalogMedia.EXTRA_AUTHORS]?.let {
		Json.decodeFromString(it)
	}

val CatalogMedia.tags: Array<CatalogTag>?
	get() = extras[CatalogMedia.EXTRA_TAGS]?.let {
		Json.decodeFromString(it)
	}

val CatalogMedia.genres: Array<String>?
	get() = extras[CatalogMedia.EXTRA_GENRES]?.let {
		Json.decodeFromString(it)
	}

val CatalogMedia.duration: Int?
	get() = extras[CatalogMedia.EXTRA_DURATION]?.toIntOrNull()

val CatalogMedia.episodes: Int?
	get() = extras[CatalogMedia.EXTRA_EPISODES]?.toIntOrNull()

@get:Composable
val CatalogMedia.episodesLocalized: String?
	get() {
		val latest = extras[CatalogMedia.EXTRA_LATEST_EPISODE]?.toIntOrNull()
		val total = episodes
		
		val postfix = (latest ?: total)?.let {
			if(it == 1) {
				stringResource(Res.string.episode)
			} else {
				stringResource(Res.string.episodes)
			}
		}
		
		if(total != null) {
			if(latest != null) {
				return "$latest/$total $postfix"
			}
			
			return "$total $postfix"
		}
		
		if(latest != null) {
			return "$latest $postfix"
		}
		
		return null
	}

val CatalogMedia.ageRating: AgeRating?
	get() = extras[CatalogMedia.EXTRA_AGE_RATING]?.let { AgeRating.match(it) }