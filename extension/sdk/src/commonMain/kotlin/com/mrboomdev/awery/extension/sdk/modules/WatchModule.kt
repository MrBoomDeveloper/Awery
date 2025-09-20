package com.mrboomdev.awery.extension.sdk.modules

import com.mrboomdev.awery.extension.sdk.Either
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Results
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.extension.sdk.WatchVariant

interface WatchModule: Module {
    suspend fun watch(media: Media, page: Int = 0): Either<Video, Results<WatchVariant>>

    suspend fun watch(watchVariant: WatchVariant, page: Int = 0): Either<Video, Results<WatchVariant>> {
        throw UnsupportedOperationException("Not implemented!")
    }
}