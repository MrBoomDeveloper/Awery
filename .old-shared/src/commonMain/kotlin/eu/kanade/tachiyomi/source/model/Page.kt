package eu.kanade.tachiyomi.source.model

import com.mrboomdev.awery.AndroidUri
import com.mrboomdev.awery.utils.ExtensionSdk

@ExtensionSdk
open class Page(
    val index: Int,
    val url: String = "",
    var imageUrl: String? = null,
    @Transient var uri: AndroidUri? = null, // Deprecated but can't be deleted due to extensions
)