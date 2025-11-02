package com.mrboomdev.awery.core.utils

import io.github.vinceglb.filekit.PlatformFile
import io.ktor.client.statement.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.RawSink
import kotlinx.io.asSink
import kotlinx.serialization.json.Json
import java.io.FileOutputStream

/**
 * Downloads the content of the HTTP response to the specified file.
 *
 * This function will suspend until the download is complete.
 *
 * @param targetFile The file to download the content to.
 */
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