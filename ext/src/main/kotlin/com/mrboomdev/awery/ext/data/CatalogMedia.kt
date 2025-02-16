package com.mrboomdev.awery.ext.data
import com.mrboomdev.awery.ext.constants.AgeRating
import com.mrboomdev.awery.ext.util.GlobalId
import kotlinx.serialization.json.Json
import java.io.Serial
import java.io.Serializable

@kotlinx.serialization.Serializable
class CatalogMedia(
	/**
	 * A unique media id in the following format:
	 * MANAGER_ID;;;SOURCE_ID;;;MEDIA_ID
	 */
	val globalId: GlobalId,
	
	/**
	 * A title, which should be shown to the user.
	 */
	val title: String,
	
	/**
	 * An collection of other useful data such as trailer, screenshots, poster and banner not impactful on search
	 */
	val extras: Map<String, String> = emptyMap(),
	
	/**
	 * An set of ids for different services like for ex:
	 * ```json
	 * { "anilist": "57375", "mal": "473829" }
	 * ```
	 */
	val ids: Map<String, String> = emptyMap()
) : Serializable {
	
	constructor(original: CatalogMedia) : this(
		globalId = original.globalId,
		title = original.title,
		ids = original.ids.toMap(),
		extras = original.extras.toMap()
	)
	
	enum class Status {
		ONGOING, COMPLETED, COMING_SOON, PAUSED, CANCELLED
	}
	
	enum class Type {
		MUSIC, VIDEO, POST, MOVIE, BOOK
	}
	
	inline val status: Status?
		get() = extras[EXTRA_STATUS]?.uppercase()?.let {
			try { Status.valueOf(it) } catch(_: Throwable) { null } 
		}
	
	inline val type: Type?
		get() = when(extras[EXTRA_TYPE]?.lowercase()) {
			"movie", "tv" -> Type.MOVIE
			"book", "comic" -> Type.BOOK
			"post" -> Type.POST
			"video" -> Type.VIDEO
			"music" -> Type.MUSIC
			else -> null
		}
	
	inline val authors: Array<String>?
		get() = extras[EXTRA_AUTHORS]?.let {
			Json.decodeFromString(it)
		}
	
	inline val tags: Array<CatalogTag>?
		get() = extras[EXTRA_TAGS]?.let {
			Json.decodeFromString(it)
		}
	
	inline val genres: Array<String>?
		get() = extras[EXTRA_GENRES]?.let { 
			Json.decodeFromString(it)
		}
	
	inline val ageRating: AgeRating?
		get() = extras[EXTRA_AGE_RATING]?.let { AgeRating.match(it) }

	companion object {
		@Serial
		private val serialVersionUID = 2L
		
		/**
		 * @param poster [EXTRA_POSTER]
		 * @param banner [EXTRA_BANNER]
		 * @param episodes [EXTRA_EPISODES]
		 * @param description [EXTRA_DESCRIPTION]
		 * @param type [EXTRA_TYPE]
		 */
		@Suppress("NOTHING_TO_INLINE")
		inline fun create(
			globalId: GlobalId,
			title: String,
			ids: Map<String, String> = emptyMap(),
			description: String? = null,
			status: Status? = null,
			alternativeTitles: Array<String>? = null,
			type: String? = null,
			score: Int? = null,
			seasons: Int? = null,
			episodes: Int? = null,
			duration: Int? = null,
			latestEpisode: Int? = null,
			country: String? = null,
			trailer: String? = null,
			poster: String? = null,
			banner: String? = null,
			shareUrl: String? = null,
			reportUrl: String? = null,
			releaseDate: Long? = null,
			endDate: Long? = null,
			externalLinks: Array<ExternalLink>? = null,
			ageRating: AgeRating? = null,
			screenshots: Array<String>? = null,
			authors: Array<String>? = null,
			statusRatings: Array<Pair<Status, Long>>? = null,
			ratings: Array<Pair<Int, Long>>? = null,
			tags: Array<CatalogTag>? = null,
			genres: Array<String>? = null,
			extras: Map<String, String> = emptyMap()
		) = CatalogMedia(
			globalId = globalId,
			title = title,
			ids = ids,
			extras = buildMap {
				putSafe(EXTRA_TAGS, tags)
				putSafe(EXTRA_GENRES, genres)
				putSafe(EXTRA_AUTHORS, authors)
				putSafe(EXTRA_EXTERNAL_LINKS, externalLinks)
				putSafe(EXTRA_AGE_RATING, ageRating?.age)
				putSafe(EXTRA_END_DATE, endDate)
				putSafe(EXTRA_RELEASE_DATE, releaseDate)
				putSafe(EXTRA_REPORT, reportUrl)
				putSafe(EXTRA_SHARE, shareUrl)
				putSafe(EXTRA_BANNER, banner)
				putSafe(EXTRA_POSTER, poster)
				putSafe(EXTRA_COUNTRY, country)
				putSafe(EXTRA_TRAILER, trailer)
				putSafe(EXTRA_SCREENSHOTS, screenshots)
				putSafe(EXTRA_LATEST_EPISODE, latestEpisode)
				putSafe(EXTRA_DURATION, duration)
				putSafe(EXTRA_EPISODES, episodes)
				putSafe(EXTRA_SEASONS, seasons)
				putSafe(EXTRA_RATINGS, ratings)
				putSafe(EXTRA_STATUS_RATINGS, statusRatings)
				putSafe(EXTRA_SCORE, score)
				putSafe(EXTRA_TYPE, type)
				putSafe(EXTRA_ALT_TITLES, alternativeTitles)
				putSafe(EXTRA_STATUS, status?.name)
				putSafe(EXTRA_DESCRIPTION, description)
				putAll(extras)
			}
		)
		
		inline fun <reified T> MutableMap<String, String>.putSafe(key: String, value: T?) {
			value?.also { put(key, when(it) {
				is String -> it
				is Number -> it.toString()
				else -> Json.encodeToString(it)
			}) }
		}
		
		/**
		 * Current releasing status of the media. Allowed values:
		 * [Status.ONGOING], [Status.COMPLETED], [Status.COMING_SOON], [Status.PAUSED], [Status.CANCELLED]
		 */
		const val EXTRA_STATUS = "STATUS"
		
		/**
		 * An description of the media
		 */
		const val EXTRA_DESCRIPTION = "DESCRIPTION"
		
		/**
		 * Alternative names of the media in the json format:
		 * ["Name in German", "Name in Spanish"]
		 */
		const val EXTRA_ALT_TITLES = "ALT_TITLES"
		
		/**
		 * A type of the media. May be:
		 * ```
		 * "movie", "tv" -> Type.MOVIE
		 * "book", "comic" -> Type.BOOK
		 * "post" -> Type.POST
		 * "video" -> Type.VIDEO
		 * "music" -> Type.MUSIC
		 * ```
		 */
		const val EXTRA_TYPE = "TYPE"
		
		/**
		 * An average score from 0 to 100
		 * Displayed number may vary depending on the max value of [EXTRA_RATINGS]
		 */
		const val EXTRA_SCORE = "SCORE"
		
		/**
		 * Like [EXTRA_RATINGS], but for lists:
		 * ```
		 * [
		 *  [ "WATCHING", 393939934 ],
		 *  [ "COMPLETED", 3932 ],
		 *  [ "PLANNING, 932 ],
		 *  [ "DELAYED", 92999 ],
		 *  [ "DROPPED", 1000 ]
		 * ]
		 * ```
		 */
		const val EXTRA_STATUS_RATINGS = "STATUS_RATINGS"
		
		/**
		 * An json with statistics of each score:
		 * ```
		 * [
		 *  [ 5, 393939934 ],
		 *  [ 4, 3932 ],
		 *  [ 3, 932 ],
		 *  [ 2, 92999 ],
		 *  [ 1, 1000 ]
		 * ]
		 * ```
		 */
		const val EXTRA_RATINGS = "RATINGS"
		
		/**
		 * An origin country where this media been made
		 */
		const val EXTRA_COUNTRY = "COUNTRY"
		
		/**
		 * An average episode duration
		 */
		const val EXTRA_DURATION = "DURATION"
		
		/**
		 * Must be an url either on the video, either on the webpage with the video embed:
		 * https://example.com/trailer.mp4 or https://example.com/trailer_embed?video=483948&autoplay=true
		 */
		const val EXTRA_TRAILER = "TRAILER"
		
		/**
		 * An json array ["https://example.com/screenshot1.png", "https://example.com/screenshot2.png"]
		 */
		const val EXTRA_SCREENSHOTS = "SCREENSHOTS"
		
		/**
		 * An image url like:
		 * https://example.com/poster.png
		 */
		const val EXTRA_POSTER = "POSTER"
		
		/**
		 * An image url like:
		 * https://example.com/banner.png
		 */
		const val EXTRA_BANNER = "BANNER"
		
		/**
		 * A link, where user can go to report a bad content:
		 * https://example.com/report?media=388484
		 */
		const val EXTRA_REPORT = "REPORT"
		
		/**
		 * A link, which can be shared with friends to the content
		 * https://example.com/media/48493
		 */
		const val EXTRA_SHARE = "SHARE"
		
		/**
		 * A date when media was released (First episode) in time after epoch
		 * 393995858588494959
		 */
		const val EXTRA_RELEASE_DATE = "RELEASE_DATE"
		
		/**
		 * An json array of authors 
		 * ```json
		 * ["MrBoomDev", "MrBoomStudios"]
		 * ```
		 */
		const val EXTRA_AUTHORS = "AUTHORS"
		
		/**
		 * An json list of external services with the same media:
		 * ```json
		 * [
		 * 	{ "title": "Twitter", "link": "https://x.com/my_awesome_page", "icon": "https://example.com/twitter_icon.png" }
		 * ]
		 * ```
		 */
		const val EXTRA_EXTERNAL_LINKS = "EXTERNAL_LINKS"
		
		/**
		 * An amount of seasons
		 */
		const val EXTRA_SEASONS = "SEASONS"
		
		/**
		 * A total amount episodes
		 */
		const val EXTRA_EPISODES = "EPISODES"
		
		/**
		 * An amount of released episodes at the moment
		 */
		const val EXTRA_LATEST_EPISODE = "LATEST_EPISODE"
		
		/**
		 * An date when the latest episode was released
		 */
		const val EXTRA_END_DATE = "END_DATE"
		
		/**
		 * An age rating of the media. Users with lower age won't be able to access it
		 */
		const val EXTRA_AGE_RATING = "AGE_RATING"
		
		/**
		 * An json array of strings:
		 * ```
		 * ["action", "adventure", "horror"]
		 * ```
		 */
		const val EXTRA_GENRES = "GENRES"
		
		/**
		 * An json array of tags:
		 * ```
		 * [
		 * 	{ "name": "Ghosts", "isSpoiler": true }
		 * ]
		 * ```
		 */
		const val EXTRA_TAGS = "TAGS"
	}
}