package com.mrboomdev.awery.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class DBMediaProgress(
	@PrimaryKey val globalId: String,
	val extras: Map<String, String> = emptyMap(),
	val lists: Array<String> = emptyArray()
) {
	companion object {
		/**
		 * An json array of integers:
		 * [1, 2, 3, 5]
		 */
		const val EXTRA_WATCHED = "WATCHED"
		
		/**
		 * An global id of the source:
		 * MANAGER_ID;;;SOURCE_ID
		 */
		const val EXTRA_LATEST_SOURCE = "LATEST_SOURCE"
		
		/**
		 * An integer
		 */
		const val EXTRA_LATEST_EPISODE = "LATEST_EPISODE"
		
		/**
		 * An long
		 */
		const val EXTRA_EPISODE_PROGRESS = "EPISODE_PROGRESS"
		
		/**
		 * An string array
		 */
		const val EXTRA_LAST_VARIANT = "LAST_VARIANT"
		
		const val EXTRA_LATEST_TITLE = "LATEST_TITLE"
		
		/**
		 * An map:
		 * ```
		 * {
		 * 	"awery.ext.anilist": "384849"
		 * }
		 * ```
		 */
		const val EXTRA_TRACKERS = "TRACKERS"
	}
}