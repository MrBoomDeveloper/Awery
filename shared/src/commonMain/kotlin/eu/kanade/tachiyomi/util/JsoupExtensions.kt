package eu.kanade.tachiyomi.util

import com.mrboomdev.awery.utils.ExtensionSdk
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

/**
 * Returns a Jsoup document for this response.
 * @param html the body of the response. Use only if the body was read before calling this method.
 */
@ExtensionSdk
fun Response.asJsoup(html: String? = null): Document {
	return Jsoup.parse(html ?: body.string(), request.url.toString())
}