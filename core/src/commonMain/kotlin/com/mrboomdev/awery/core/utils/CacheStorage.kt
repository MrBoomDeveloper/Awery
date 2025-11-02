package com.mrboomdev.awery.core.utils

import com.mayakapps.kache.FileKache
import com.mayakapps.kache.KacheStrategy
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.lang.System.currentTimeMillis

/**
 * Creates a new instance of [CacheStorage].
 *
 * @param directory The directory in which the cache files will be stored.
 * @param maxSize The maximum size of the cache in bytes.
 * @param strategy The strategy to use when storing and retrieving cache values.
 * If not specified, defaults to [CacheStorage.Strategy.LRU].
 * @param maxAge The maximum age of a cache value in milliseconds.
 * If not specified, defaults to [Long.MAX_VALUE].
 *
 * @return A new instance of [CacheStorage].
 */
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