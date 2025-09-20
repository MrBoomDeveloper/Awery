package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier

@Composable
actual fun WebBrowser(
    modifier: Modifier,
    state: WebBrowserState
) {
}

actual class WebBrowserState actual constructor(initialUrl: String) {
    private val _url = mutableStateOf(initialUrl)
    actual val url by _url
    
    private val _progress = mutableStateOf(0f)
    actual val progress by _progress

    actual fun loadUrl(url: String) {
        
    }
    
    actual fun reload() {
        
    }

    actual companion object
}