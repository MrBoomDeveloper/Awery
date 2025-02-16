package com.mrboomdev.awery.ext.constants

@JvmInline
value class AgeRating(val age: Int) {
	companion object {
		val EVERYONE = AgeRating(0)
		val NSFW = AgeRating(18)
		
		fun match(string: String): AgeRating? {
			string.substringBefore("+").toIntOrNull()?.also {
				return AgeRating(it)
			}
			
			return when(string.lowercase()) {
				"everyone", "e" -> EVERYONE
				"nsfw" -> NSFW
				else -> null
			}
		}
	}
}