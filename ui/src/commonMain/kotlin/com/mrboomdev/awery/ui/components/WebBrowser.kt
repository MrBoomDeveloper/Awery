package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

@Composable
fun rememberWebBrowserState(
    initialUrl: String
): WebBrowserState {
    return rememberSaveable(
        saver = WebBrowserState.Saver
    ) { WebBrowserState(initialUrl) }
}

@Composable
expect fun WebBrowser(
    modifier: Modifier,
    state: WebBrowserState
)

expect class WebBrowserState(initialUrl: String) {
    val url: String
    val progress: Float
    fun loadUrl(url: String)
    fun reload()
    companion object
}

val WebBrowserState.Companion.Saver
    get() = mapSaver(
        save = {
            buildMap { 
                put("url", it.url)
            }
        },
        
        restore = {
            WebBrowserState(
                initialUrl = it["url"] as String
            )
        }
    )