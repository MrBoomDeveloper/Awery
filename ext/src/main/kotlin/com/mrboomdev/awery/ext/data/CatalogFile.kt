package com.mrboomdev.awery.ext.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
open class CatalogFile(
	open val url: String,
	open val title: String? = null,
	open val headers: Map<String, String>? = null,
	open val locale: String? = null
)

@Serializable
class CatalogVideoFile private constructor(
	@Transient private val _url: String = "",
	@Transient private val _title: String? = null,
	@Transient private val _headers: Map<String, String>? = null,
	@Transient private val _locale: String? = null,
	val quality: Int?,
	val audioTracks: List<CatalogFile>?,
	val videoTracks: List<CatalogFile>?,
	val subtitleTracks: List<CatalogFile>?,
	@Transient private val __UNIQUE_CONSTRUCTOR_QUALIFIER__: Boolean = false
): CatalogFile(
	_url, _title, _headers, _locale
) {
	constructor(
		url: String,
		title: String? = null,
		headers: Map<String, String>? = null,
		locale: String? = null,
		quality: Int? = null,
		audioTracks: List<CatalogFile>? = null,
		videoTracks: List<CatalogFile>? = null,
		subtitleTracks: List<CatalogFile>? = null
	): this(
		url,
		title,
		headers,
		locale,
		quality,
		audioTracks,
		videoTracks,
		subtitleTracks,
		true
	)
}