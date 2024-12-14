package com.mrboomdev.awery.ext.constants

/**
 * Use values from this enum to let an app properly filter content by it's age rating.
 */
enum class AweryAgeRating(private val userReadableString: String) {
	EVERYONE("0+"),
	THREE_PLUS("3+"),
	SIX_PLUS("6+"),
	SEVEN_PLUS("7+"),
	TWELVE_PLUS("12+"),
	THIRTEEN_PLUS("13+"),
	SIXTEEN_PLUS("16+"),
	EIGHTEEN_PLUS("18+"),
	NSFW("NSFW");

	override fun toString(): String {
		return userReadableString
	}}