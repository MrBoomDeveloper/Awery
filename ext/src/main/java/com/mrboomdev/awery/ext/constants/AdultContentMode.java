package com.mrboomdev.awery.ext.constants;

public enum AdultContentMode {
	/**
	 * Tell user that the content is mostly NSFW
	 */
	ONLY,

	/**
	 * The content is being moderated and is safe for kids
	 */
	NONE,

	/**
	 * There is such content but it always marked as NSFW
	 */
	MARKED
}