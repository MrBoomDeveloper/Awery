package com.mrboomdev.awery.core.utils

import com.mayakapps.kache.FileKache
import com.mayakapps.kache.KacheStrategy
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.System.currentTimeMillis

suspend inline fun <reified T> CacheStorage(
    directory: PlatformFile,
    maxSize: Long,
    strategy: CacheStorage.Strategy = CacheStorage.Strategy.LRU,
    maxAge: Long = Long.MAX_VALUE
) = CacheStorage(
    serializer = serializer<T>(),
    maxAge = maxAge,
    impl = FileKache(directory.absolutePath(), maxSize) {
        this.strategy = strategy.impl
    }
)

class CacheStorage<T> @PublishedApi internal constructor(
    private val impl: FileKache,
    private val serializer: KSerializer<T>,
    private val maxAge: Long
) {
    suspend operator fun get(key: String): T? {
        return impl.get(key)
            ?.let { path -> 
                PlatformFile(path).also {
                    if(maxAge < Long.MAX_VALUE && it.toJavaFile().lastModified() + maxAge < currentTimeMillis()) {
                        impl.remove(key)
                        return null
                    }
                }.readString()
            }?.let { json -> Json.decodeFromString(serializer, json) }
    }

    suspend operator fun set(key: String, value: T) {
        impl.put(key) {
            PlatformFile(it).writeString(Json.encodeToString(serializer, value))
            true
        }
    }

    enum class Strategy(
        @PublishedApi internal val impl: KacheStrategy
    ) {
        LRU(KacheStrategy.LRU),
        MRU(KacheStrategy.MRU),
        FIFO(KacheStrategy.FIFO),
        FILO(KacheStrategy.FILO)
    }
}