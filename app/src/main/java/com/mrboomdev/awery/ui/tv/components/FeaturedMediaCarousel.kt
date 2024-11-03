package com.mrboomdev.awery.ui.tv.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
	@Composable
	fun Modifier.onFirstGainingVisibility(onGainingVisibility: () -> Unit): Modifier {
		var isVisible by remember { mutableStateOf(false) }
		LaunchedEffect(isVisible) { if (isVisible) onGainingVisibility() }

		return onPlaced { isVisible = true }
	}

	@Composable
	fun Modifier.requestFocusOnFirstGainingVisibility(): Modifier {
		val focusRequester = remember { FocusRequester() }
		return focusRequester(focusRequester).onFirstGainingVisibility {
			focusRequester.requestFocus()
		}
	}

	var carouselFocused by remember { mutableStateOf(false) }

	Carousel(
		itemCount = featuredContent.size,
		modifier = modifier.onFocusChanged {
			carouselFocused = it.isFocused
		},

		contentTransformEndToStart = fadeIn(tween(500))
			.togetherWith(fadeOut(tween(500))),

		contentTransformStartToEnd = fadeIn(tween(500))
			.togetherWith(fadeOut(tween(500)))
	) { index ->
		val media = featuredContent[index]

		Box {
			var buttonFocused by remember { mutableStateOf(false) }

			val buttonModifier = if(!carouselFocused) Modifier
			else Modifier.requestFocusOnFirstGainingVisibility()

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

				Spacer(modifier = Modifier.height(32.dp))

				Row(
					modifier = Modifier.fillMaxHeight(),
					verticalAlignment = Alignment.Bottom
				) {
					Button(
						onClick = { onItemSelected(media) },
						modifier = buttonModifier
							.onFocusChanged { buttonFocused = it.isFocused }
					) {
						Text(
							style = MaterialTheme.typography.bodyLarge,
							text = stringResource(R.string.watch_now)
						)
					}

					Spacer(modifier = Modifier.width(16.dp))

					Button(
						onClick = { toast("Coming soon...") }
					) {
						Text(
							style = MaterialTheme.typography.bodyLarge,
							text = stringResource(R.string.bookmark)
						)
					}
				}
			}
		}
	}
}