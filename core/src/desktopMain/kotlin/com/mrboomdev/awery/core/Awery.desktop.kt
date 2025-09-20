package com.mrboomdev.awery.core

import com.mrboomdev.awery.core.utils.toJavaFile
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.resolve
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI

actual object Awery {
    actual fun copyToClipboard(text: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(
            StringSelection(text), null
        )
    }
    
    actual fun openUrl(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }

    actual val isTv = false

    actual val defaultUserAgent: String
        get() = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/${fallbackChromeVersion} Safari/537.36"
    
    actual val platform get() = Platform.DESKTOP

    actual fun share(text: String) {
        copyToClipboard(text)
    }
}

internal actual fun HttpClientConfig<*>.setup() {
    install(HttpCache) {
        publicStorage(FileStorage(FileKit.cacheDir.resolve("http/public").toJavaFile()))
        privateStorage(FileStorage(FileKit.cacheDir.resolve("http/private").toJavaFile()))
    }
}