package com.mrboomdev.awery.core.utils

import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.RawSink
import kotlinx.serialization.json.Json
import kotlinx.io.asSink
import java.io.FileOutputStream

suspend fun HttpStatement.download(targetFile: PlatformFile) {
    execute { httpResponse ->
        val stream: RawSink = FileOutputStream(targetFile.toJavaFile()).asSink()
        val channel = httpResponse.bodyAsChannel()
        var count = 0L
        
        stream.use {
            while(!channel.exhausted()) {
                val chunk = channel.readRemaining()
                count += chunk.remaining
                
                chunk.transferTo(stream)
            }
        }
    }
}

@PublishedApi
internal val defaultJsonSpec = Json { 
    ignoreUnknownKeys = true
}

suspend inline fun <reified T> HttpResponse.bodyAsJson() =
    defaultJsonSpec.decodeFromString<T>(bodyAsText())