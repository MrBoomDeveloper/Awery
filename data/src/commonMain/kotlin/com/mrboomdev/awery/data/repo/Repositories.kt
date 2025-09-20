package com.mrboomdev.awery.data.repo

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.http
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.data.repo.Repository
import com.mrboomdev.awery.data.database.entity.DBRepository
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

object Repositories {
	suspend fun fetch(url: String): Repository {
		val response = Awery.http.get(url).bodyAsText()
		
		try {
			return YomiRepository.parse(url, response)
		} catch(e: Exception) {
			Log.e("Repositories", "Failed to parse an yomi repository!", e)
			
			try {
				return CloudstreamRepository.parse(url, response)
			} catch(e2: Exception) {
				Log.e("Repositories", "Failed to parse an cloudstream repository!", e2)
			}
		}
		
		throw InvalidRepositoryException("Unsupported repository format!")
	}
}

class InvalidRepositoryException(
	message: String
): Exception(message)