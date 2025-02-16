package com.mrboomdev.awery.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.data.CatalogMedia

enum class MediaScreenTab {
	INFO,
	PLAY,
	COMMENTS,
	RELATIONS
}

@Composable
fun MediaScreen(
	media: CatalogMedia,
	initialTab: MediaScreenTab
) {
	Text("Media screen")
}