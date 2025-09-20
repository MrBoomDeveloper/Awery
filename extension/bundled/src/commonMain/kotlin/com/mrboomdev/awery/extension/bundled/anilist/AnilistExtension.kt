package com.mrboomdev.awery.extension.bundled.anilist

import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistRequest
import com.mrboomdev.awery.extension.bundled.anilist.entity.AnilistResponse
import com.mrboomdev.awery.extension.bundled.anilist.query.AnilistQuery
import com.mrboomdev.awery.extension.sdk.Context
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.ExtensionLoadException
import com.mrboomdev.awery.extension.sdk.Image
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class AnilistExtension(private val context: Context): Extension {
    override val name = "Anilist"
    override val id = ID
    override val webpage = "https://anilist.co"
    override val version = "1.0.0"
    override val lang = "en"
    override val isNsfw = false
    override val icon = null
    override val loadException = null

    override fun createModules() = listOf(
        AnilistCatalog, AnilistPreferences(context.preferences)
    )

    companion object {
        const val ID = "jvm_com.mrboomdev.awery.extension.anilist"
        
        val httpClient = HttpClient(CIO) {
            expectSuccess = true
            ContentEncoding()

            defaultRequest {
                url("https://graphql.anilist.co")
                contentType(ContentType.Application.Json)
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
        
        suspend fun <T> query(query: AnilistQuery<T>): T {
            return httpClient.post {
                setBody(Json.encodeToString(AnilistRequest(query)))
            }.bodyAsText().let {
                query.getResult(Json.decodeFromString<AnilistResponse>(it))
            }
        }
    }
}