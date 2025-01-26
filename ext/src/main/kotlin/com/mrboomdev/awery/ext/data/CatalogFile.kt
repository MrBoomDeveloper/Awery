package com.mrboomdev.awery.ext.data

import kotlinx.serialization.Serializable

@Serializable
open class CatalogFile(
	open val url: String,
	open val title: String? = null,
	open val headers: Map<String, String>? = null,
	open val locale: String? = null
)

@Serializable
class CatalogVideoFile private constructor(
	private val _url: String,
	private val _title: String?,
	private val _headers: Map<String, String>?,
	private val _locale: String?,
	val quality: Int?,
	val audioTracks: List<CatalogFile>?,
	val videoTracks: List<CatalogFile>?,
	val subtitleTracks: List<CatalogFile>?,
	private val __UNIQUE_CONSTRUCTOR_QUALIFIER__: Boolean
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