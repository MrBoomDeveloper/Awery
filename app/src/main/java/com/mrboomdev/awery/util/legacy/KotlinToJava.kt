package com.mrboomdev.awery.util.legacy

import ani.awery.client
import com.lagradost.nicehttp.NiceResponse

object KotlinToJava {

    suspend fun clientPost(
        url: String,
        headers: Map<String, String>,
        data: Map<String, String>,
        cacheTime: Int
    ): NiceResponse {
        return client.post(
            url,
            headers,
            data = data,
            cacheTime = cacheTime
        )
    }
}