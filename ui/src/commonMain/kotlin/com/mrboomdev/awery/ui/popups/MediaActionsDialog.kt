package com.mrboomdev.awery.ui.popups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import be.digitalia.compose.htmlconverter.htmlToString
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.mrboomdev.awery.core.utils.replaceAll
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.bookmark
import com.mrboomdev.awery.resources.done
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.resources.ic_done
import com.mrboomdev.awery.resources.poster_no_image
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MediaActionsDialog(
	extensionId: String,
	media: Media,
	onDismissRequest: () -> Unit
) {
	var showBookmarkDialog by remember { mutableStateOf(false) }
	
	BottomSheetDialog(onDismissRequest) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			horizontalArrangement = Arrangement.spacedBy(24.dp)
		) { 
			media.getPoster()?.also { poster ->
				AsyncImage(
					modifier = Modifier
						.clip(RoundedCornerShape(10.dp))
						.fillMaxWidth(.3f)
						.aspectRatio(7F / 10F)
						.background(MaterialTheme.colorScheme.surfaceContainerLow),

					model = ImageRequest.Builder(LocalPlatformContext.current)
						.placeholderMemoryCacheKey(poster)
						.memoryCacheKey(poster)
						.data(poster)
						.build(),

					error = painterResource(Res.drawable.poster_no_image),
					contentScale = ContentScale.Crop,
					contentDescription = null
				)
			}

			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(6.dp)
			) {
				Text(
					style = MaterialTheme.typography.titleLarge,
					text = media.title
				)
				
				media.description?.also { description ->
					Text(
						overflow = TextOverflow.Ellipsis,
						maxLines = 3,
						text = remember(description) {
							htmlToString(
								html = description.trim(),
								compactMode = true
							).replaceAll("\n\n", "\n")
						}
					)
				}

				TextButton(
					modifier = Modifier.fillMaxWidth(),
					contentPadding = PaddingValues(vertical = 12.dp),
					onClick = { showBookmarkDialog = true }
				) {
					Icon(
						modifier = Modifier.size(22.dp),
						painter = painterResource(Res.drawable.ic_collections_bookmark_outlined),
						contentDescription = null
					)

					Text(
						modifier = Modifier
							.padding(horizontal = 8.dp)
							.weight(1f),
						text = stringResource(Res.string.bookmark)
					)
				}
			}
		}
	}
	
	if(showBookmarkDialog) {
		BookmarkMediaDialog(
			extensionId = extensionId,
			media = media,
			onDismissRequest = { 
				onDismissRequest()
				showBookmarkDialog = false 
			}
		)
	}
}