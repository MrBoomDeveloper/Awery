package com.mrboomdev.awery.ui.tv.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.ext.data.CatalogMedia

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun FeaturedMediaCarousel(
	featuredContent: List<CatalogMedia>,
	modifier: Modifier = Modifier,
	onItemSelected: (media: CatalogMedia) -> Unit = {}
) {
	Carousel(
		modifier = modifier,
		itemCount = featuredContent.size,

		contentTransformEndToStart = fadeIn(tween(500))
			.togetherWith(fadeOut(tween(500))),

		contentTransformStartToEnd = fadeIn(tween(500))
			.togetherWith(fadeOut(tween(500)))
	) { index ->
		val media = featuredContent[index]

		Box(
			modifier = Modifier.clickable { onItemSelected(media) }
		) {
			AsyncImage(
				model = media.banner,
				contentDescription = media.description,
				contentScale = ContentScale.Crop,
				modifier = Modifier.fillMaxSize()
			)

			Column(
				modifier = Modifier
					.fillMaxHeight()
					.padding(64.dp)
			) {
				Text(
					style = MaterialTheme.typography.displayMedium,
					text = media.title ?: "No title"
				)

				Spacer(modifier = Modifier.height(12.dp))

				Text(
					style = MaterialTheme.typography.bodyLarge,
					text = media.description ?: "No description"
				)
			}
		}
	}
}