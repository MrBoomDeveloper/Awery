package com.mrboomdev.awery.ui.popups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToString
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.replaceAll
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.DBBlacklistedMedia
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.utils.only
import com.mrboomdev.awery.ui.utils.padding
import com.mrboomdev.awery.ui.utils.start
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaActionsDialog(
	extensionId: String,
	media: Media,
	onHide: (() -> Unit)? = null,
	onCancelledHide: (() -> Unit)? = null,
	onDismissRequest: () -> Unit
) {
	val toaster = LocalToaster.current
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
						.padding(top = 16.dp, start = contentPadding.start + 32.dp, end = 16.dp, bottom = 6.dp),
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
					.padding(horizontal = 32.dp, top = 6.dp),
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
							verticalArrangement = Arrangement.spacedBy(6.dp)
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

				if(onHide != null) {
					Item(
						icon = painterResource(Res.drawable.ic_block),
						text = stringResource(Res.string.hide),
						onClick = {
							launchGlobal(Dispatchers.IO) {
								Awery.database.mediaBlacklist.add(DBBlacklistedMedia(
									extensionId = extensionId,
									mediaId = media.id,
									name = media.title
								))

								if(onCancelledHide != null) {
									toaster.toast(
										title = "${media.title} was hidden",
										actionText = getString(Res.string.cancel),
										onClick = {
											launchGlobal(Dispatchers.IO) {
												Awery.database.mediaBlacklist.delete(DBBlacklistedMedia(
													extensionId = extensionId,
													mediaId = media.id,
													name = media.title
												))
											}
											
											onCancelledHide()
										}
									)
								}
							}

							onHide()
						}
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