package com.mrboomdev.awery.app

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.decode.DataSource
import coil3.decode.ImageSource
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.SourceFetchResult
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.size.Precision
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.toJavaFile
import com.mrboomdev.awery.core.utils.useTemporaryFile
import com.mrboomdev.awery.extension.loaders.Extensions
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.github.vinceglb.filekit.resolve
import kotlinx.coroutines.Dispatchers
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

fun Awery.initEverything() {
    platformInit()
    useTemporaryFile {}
    
    SingletonImageLoader.setSafe { context ->
        ImageLoader.Builder(context).apply {
//            networkCachePolicy(CachePolicy.ENABLED)
//            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(true)
            
            components {
                addPlatformFileSupport()
            }

//            diskCache {
//                DiskCache.Builder().apply {
//                    directory(FileKit.cacheDir.resolve("coil").toJavaFile())
//                    maxSizeBytes(500 * 1024 * 1024 * 8)
//                    maxSizePercent(.25)
//                }.build()
//            }
        }.build()
    }

    launchGlobal(Dispatchers.Default) {
        Extensions.loadAll()
    }
}

internal expect fun platformInit()