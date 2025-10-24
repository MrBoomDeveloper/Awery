package com.mrboomdev.awery.data

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.http
import com.mrboomdev.awery.core.utils.bodyAsJson
import com.mrboomdev.awery.extension.sdk.Results
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.appendPathSegments
import kotlinx.serialization.Serializable

object AweryServer {
    suspend fun getNotifications(token: String, page: Int) = Awery.http.get {
        url("http", "awery.mrboomdev.ru") {
            appendPathSegments("api", "notifications", token.takeIf { it.isNotBlank() } ?: "0", page.toString())
        }
    }.bodyAsJson<Results<AweryServerNotification>>()
}

@Serializable
data class AweryServerNotification(
    val title: String,
    val message: String,
    val date: Long
)