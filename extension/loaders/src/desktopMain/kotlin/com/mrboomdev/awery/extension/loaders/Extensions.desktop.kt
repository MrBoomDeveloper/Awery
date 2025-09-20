package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.extension.sdk.Extension
import kotlinx.coroutines.channels.ProducerScope

actual suspend fun ProducerScope<Extension>.loadAllImpl() {
    println("Desktop doesn't support extensions loading currently!")
}