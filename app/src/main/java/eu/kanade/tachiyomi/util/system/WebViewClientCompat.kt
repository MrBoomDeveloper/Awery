package eu.kanade.tachiyomi.util.system

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

@Suppress("OVERRIDE_DEPRECATION")
abstract class WebViewClientCompat : WebViewClient() {

    /**
     * Give the host application a chance to take control when a URL is about to be loaded in the
     * current WebView. If a WebViewClient is not provided, by default WebView will ask Activity
     * Manager to choose the proper handler for the URL. If a WebViewClient is provided, returning
     * {@code true} causes the current WebView to abort loading the URL, while returning
     * {@code false} causes the WebView to continue loading the URL as usual.
     *
     * <p class="note"><b>Note:</b> Do not call {@link WebView#loadUrl(String)} with the same
     * URL and then return {@code true}. This unnecessarily cancels the current load and starts a
     * new load with the same URL. The correct way to continue loading a given URL is to simply
     * return {@code false}, without calling {@link WebView#loadUrl(String)}.
     *
     * <p class="note"><b>Note:</b> This method is not called for POST requests.
     *
     * <p class="note"><b>Note:</b> This method may be called for subframes and with non-HTTP(S)
     * schemes; calling {@link WebView#loadUrl(String)} with such a URL will fail.
     *
     * @param view The WebView that is initiating the callback.
     * @param url The URL to be loaded.
     * @return {@code true} to cancel the current load, otherwise return {@code false}.
     */
    open fun shouldOverrideUrlCompat(
        view: WebView,
        url: String
    ) = false

    /**
     * Notify the host application of a resource request and allow the
     * application to return the data.  If the return value is {@code null}, the WebView
     * will continue to load the resource as usual.  Otherwise, the return
     * response and data will be used.
     *
     * <p>This callback is invoked for a variety of URL schemes (e.g., {@code http(s):}, {@code
     * data:}, {@code file:}, etc.), not only those schemes which send requests over the network.
     * This is not called for {@code javascript:} URLs, {@code blob:} URLs, or for assets accessed
     * via {@code file:///android_asset/} or {@code file:///android_res/} URLs.
     *
     * <p>In the case of redirects, this is only called for the initial resource URL, not any
     * subsequent redirect URLs.
     *
     * <p class="note"><b>Note:</b> This method is called on a thread
     * other than the UI thread so clients should exercise caution
     * when accessing private data or the view system.
     *
     * <p class="note"><b>Note:</b> When Safe Browsing is enabled, these URLs still undergo Safe
     * Browsing checks. If this is undesired, you can use {@link WebView#setSafeBrowsingWhitelist}
     * to skip Safe Browsing checks for that host or dismiss the warning in {@link
     * #onSafeBrowsingHit} by calling {@link SafeBrowsingResponse#proceed}.
     *
     * @param view The {@link android.webkit.WebView} that is requesting the
     *             resource.
     * @param url The raw url of the resource.
     * @return A {@link android.webkit.WebResourceResponse} containing the
     *         response information or {@code null} if the WebView should load the
     *         resource itself.
     */
    open fun shouldInterceptRequestCompat(
        view: WebView,
        url: String
    ): WebResourceResponse? = null

    open fun onReceivedErrorCompat(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String,
        isMainFrame: Boolean
    ) {}

    final override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ) = shouldOverrideUrlCompat(view, request.url.toString())

    final override fun shouldOverrideUrlLoading(
        view: WebView,
        url: String
    ) = shouldOverrideUrlCompat(view, url)

    final override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ) = shouldInterceptRequestCompat(view, request.url.toString())

    final override fun shouldInterceptRequest(
        view: WebView,
        url: String
    ) = shouldInterceptRequestCompat(view, url)

    final override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) = onReceivedErrorCompat(
        view,
        error.errorCode,
        error.description?.toString(),
        request.url.toString(),
        request.isForMainFrame
    )

    final override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String
    ) = onReceivedErrorCompat(
        view,
        errorCode,
        description,
        failingUrl,
        failingUrl == view.url
    )

    final override fun onReceivedHttpError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceResponse
    ) = onReceivedErrorCompat(
        view,
        error.statusCode,
        error.reasonPhrase,
        request.url.toString(),
        request.isForMainFrame
    )
}