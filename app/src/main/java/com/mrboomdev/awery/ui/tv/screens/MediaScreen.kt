package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.mrboomdev.awery.R
import com.mrboomdev.awery.ext.data.CatalogMedia
import kotlinx.serialization.Serializable

@Serializable
data class MediaScreenArgs(val media: CatalogMedia)

@Composable
fun MediaScreen(
	media: CatalogMedia
) {
	Box(
		modifier = Modifier.fillMaxSize()
	) {
		AsyncImage(
			model = media.banner,
			contentDescription = null
		)

		Column {
			Text(
				style = MaterialTheme.typography.displayMedium,
				text = media.title ?: "No title"
			)

			Text(
				style = MaterialTheme.typography.displayMedium,
				text = media.description ?: "No description"
			)

			Button(onClick = {

			}) {
				Text(stringResource(R.string.watch_now))
			}
		}
	}
}