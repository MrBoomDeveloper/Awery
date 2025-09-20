package com.mrboomdev.awery.ui.utils

import java.util.Locale

fun String.formatAsUrl(): String {
	var url = this
		.substringBefore("#")
		.substringBefore("?")
	
	if(url.startsWith("http://")) {
		url = url.substringAfter("http://")
	}

	if(url.startsWith("https://")) {
		url = url.substringAfter("https://")
	}

	if(url.startsWith("www.")) {
		url = url.substringAfter("www.")
	}
	
	if(url.endsWith("/")) {
		url = url.substringBeforeLast("/")
	}
	
	return url
}

fun String.formatAsCountry(): String {
	return formatAsLocale { displayCountry }
}

fun String.formatAsLanguage(): String {
	return formatAsLocale { displayLanguage }
}

private fun String.formatAsLocale(field: Locale.() -> String): String {
	fun get(input: String) = field(Locale.forLanguageTag(input))

	for(result in listOf(get(this), get("$this-$this"), this)) {
		if(result.isNotBlank()) {
			return result.replaceFirstChar { it.uppercaseChar() }
		}
	}

	return ""
}