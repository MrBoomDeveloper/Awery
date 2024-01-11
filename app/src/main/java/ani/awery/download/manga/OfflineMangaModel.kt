package ani.awery.download.manga

import android.net.Uri

data class OfflineMangaModel(
    val title: String,
    val score: String,
    val isOngoing: Boolean,
    val isUserScored: Boolean,
    val image: Uri?
)