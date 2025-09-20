package com.mrboomdev.awery.data.repo

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.http
import com.mrboomdev.awery.data.database.entity.DBRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object YomiRepository {
	private val jsonFormat = Json {
		ignoreUnknownKeys = true
	}
	
	suspend fun parse(url: String, json: String): Repository {
		val baseUrl = url.substringBeforeLast("/")
		
		val meta = jsonFormat.decodeFromString<MetaWrapper>(
			Awery.http.get("$baseUrl/repo.json").bodyAsText()
		).meta
		
		return Repository(
			info = DBRepository(
				extensionId = "yomi",
				url = url,
				name = meta.name
			),
			
			items = jsonFormat.decodeFromString<List<Item>>(json).map { 
				it.toRepoItem(baseUrl)
			}
		)
	}
	
	@Serializable
	private data class MetaWrapper(
		val meta: Meta
	)
	
	@Serializable
	private data class Meta(
		val name: String
	)
	
	@Serializable
	private data class Item(
		val name: String,
		val pkg: String,
		val apk: String,
		val lang: String,
		val version: String,
		val nsfw: Int,
		val sources: List<Source>
	) {
		@Serializable
		data class Source(
			val baseUrl: String
		)
	}

	private fun Item.toRepoItem(
		baseUrl: String
	) = Repository.Item(
		id = "yomi_$pkg",
		name = name.substringAfter("Aniyomi: ").substringAfter("Tachiyomi: "),
		version = version,
		url = "$baseUrl/apk/$apk",
		icon = "$baseUrl/icon/$pkg.png",
		isNsfw = nsfw != 0,
		lang = lang
	)
}