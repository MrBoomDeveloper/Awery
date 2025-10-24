package com.mrboomdev.awery.ui.popups

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.SmallExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutBoundsHolder
import androidx.compose.ui.layout.layoutBounds
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import be.digitalia.compose.htmlconverter.htmlToString
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.replaceAll
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.bookmark
import com.mrboomdev.awery.resources.close
import com.mrboomdev.awery.resources.done
import com.mrboomdev.awery.resources.hide
import com.mrboomdev.awery.resources.ic_add
import com.mrboomdev.awery.resources.ic_block
import com.mrboomdev.awery.resources.ic_close
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.resources.ic_done
import com.mrboomdev.awery.resources.ic_share_filled
import com.mrboomdev.awery.resources.new_list
import com.mrboomdev.awery.resources.poster_no_image
import com.mrboomdev.awery.resources.share
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.only
import com.mrboomdev.awery.ui.utils.padding
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.singleStickyHeader
import com.mrboomdev.awery.ui.utils.start
import com.mrboomdev.awery.ui.utils.thenIf
import com.mrboomdev.awery.ui.utils.toDp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.awt.SystemColor.text

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaActionsDialog(
	extensionId: String,
	media: Media,
	onDismissRequest: () -> Unit
) {
	var showBookmarkDialog by remember { mutableStateOf(false) }

	BottomSheetDialog(onDismissRequest) {
		val contentPadding = WindowInsets.safeContent.only(
			WindowInsetsSides.Vertical
		).asPaddingValues()

		Column(
			modifier = Modifier
				.fillMaxWidth()
//				.verticalScroll(rememberScrollState())
				.padding(contentPadding.only(bottom = true))
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(16.dp)
			) {
				Text(
					modifier = Modifier
						.weight(1f)
						.padding(top = 16.dp, start = contentPadding.start + 32.dp, end = 16.dp, bottom = 4.dp),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onBackground,
					fontWeight = FontWeight.Normal,
					text = media.title
				)

				IconButton(
					modifier = Modifier
						.padding(top = 8.dp, end = 16.dp)
						.size(48.dp),
					painter = painterResource(Res.drawable.ic_close),
					contentDescription = stringResource(Res.string.close),
					onClick = onDismissRequest
				)
			}
				
			media.description?.also { description ->
				Text(
					modifier = Modifier
						.padding(contentPadding.only(horizontal = true))
						.padding(horizontal = 32.dp, bottom = 4.dp),
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

			VerticalGrid(
				modifier = Modifier
					.fillMaxWidth()
					.padding(contentPadding.only(horizontal = true))
					.padding(horizontal = 32.dp, top = 8.dp),
				columns = SimpleGridCells.Adaptive(minSize = 100.dp)
			) {
				@Composable
				fun Item(
					icon: Painter,
					text: String,
					onClick: () -> Unit
				) {
					CompositionLocalProvider(
						LocalContentColor provides MaterialTheme.colorScheme.primary
					) {
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.clip(RoundedCornerShape(16.dp))
								.clickable(onClick = onClick)
								.padding(16.dp),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.spacedBy(4.dp)
						) {
							Icon(
								modifier = Modifier.size(32.dp),
								painter = icon,
								contentDescription = null
							)

							Text(
								style = MaterialTheme.typography.bodyMedium,
								text = text
							)
						}
					}
				}
				
				Item(
					icon = painterResource(Res.drawable.ic_collections_bookmark_outlined),
					text = stringResource(Res.string.bookmark),
					onClick = { showBookmarkDialog = true }
				)
				
				media.url?.also { url ->
					Item(
						icon = painterResource(Res.drawable.ic_share_filled),
						text = stringResource(Res.string.share),
						onClick = { Awery.share(url) }
					)
				}

//				Item(
//					icon = painterResource(Res.drawable.ic_block),
//					text = stringResource(Res.string.hide),
//					onClick = { 
//						
//					}
//				)
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