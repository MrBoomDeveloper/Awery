package com.mrboomdev.awery.ext.constants

enum class AweryFeature {
	SEARCH_MEDIA, SEARCH_SUBTITLES,
	CUSTOM_SETTINGS,
	TRACKING,
	WATCH_MEDIA, READ_MEDIA,
	VOTE, LIKE, DISLIKE,
	BAN, BLOCK,
	COMMENTS, COMMENT,
	FEEDS,

	/**
	 * An manager is able to parse and download sources from external repositories
	 */
	INSTALL_REPOSITORY,

	/**
	 * An manager is able to install sources from an device storage by picking files
	 */
	INSTALL_STORAGE
}