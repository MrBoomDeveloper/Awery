package com.mrboomdev.awery.ui.components

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.mrboomdev.awery.core.Awery
import eu.kanade.tachiyomi.util.system.setDefaultSettings

@Composable
actual fun WebBrowser(
    modifier: Modifier,
    state: WebBrowserState
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    LaunchedEffect(state) {
        state.init(webView)
    }

    AndroidView(
        modifier = modifier,
        factory = { webView }
    )
}

actual class WebBrowserState actual constructor(initialUrl: String) {
    private val _url = mutableStateOf(initialUrl)
    actual val url by _url
    
    private val _progress = mutableStateOf(0f)
    actual val progress by _progress
    
    private var webView: WebView? = null
    
    internal fun init(webView: WebView) {
        this.webView = webView
        webView.setDefaultSettings()
        webView.settings.userAgentString = Awery.defaultUserAgent
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: Bitmap?
            ) {
                if(url != null) {
                    _url.value = url
                }
                
                _progress.value = 0f
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if(url != null) {
                    _url.value = url
                }
                
                _progress.value = 1f
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                return url.startsWith("intent://")
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                return shouldOverrideUrlLoading(view, request.url.toString())
            }
        }
        
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                _progress.value = newProgress.toFloat() / 100f
            }
        }

        webView.loadUrl(_url.value)
    }

    actual fun loadUrl(url: String) {
        _url.value = url
        _progress.value = 0f
        webView!!.loadUrl(url)
    }
    
    actual fun reload() {
        _progress.value = 0f
        webView!!.reload()
    }
    
    actual companion object {}
}