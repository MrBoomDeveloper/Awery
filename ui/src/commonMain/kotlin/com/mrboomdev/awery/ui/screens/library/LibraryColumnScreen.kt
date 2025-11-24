package com.mrboomdev.awery.ui.screens.library

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.database.entity.DBMedia
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.empty_library_message
import com.mrboomdev.awery.resources.empty_library_title
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.navigation.Routes
import com.mrboomdev.awery.ui.components.FeedRow
import com.mrboomdev.awery.ui.components.InfoBox
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.utils.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LibraryColumnScreen(
	viewModel: LibraryViewModel = viewModel { LibraryViewModel() },
	contentPadding: PaddingValues
) {
	val isNoLists by viewModel.isNoLists.collectAsState()
	val (didLoadLists, lists) = viewModel.lists.collectAsState().value
	val navigation = Navigation.current()

	Crossfade(when {
		isNoLists -> LibraryStatus.EMPTY
		!didLoadLists -> LibraryStatus.LOADING
		else -> LibraryStatus.LOADED
	}) { status ->
		when(status) {
			LibraryStatus.LOADING -> {
				CircularProgressIndicator(
					modifier = Modifier
						.fillMaxSize()
						.wrapContentSize()
				)
			}

			LibraryStatus.EMPTY -> {
				InfoBox(
					modifier = Modifier
						.fillMaxSize()
						.wrapContentSize(Alignment.Center),
					icon = painterResource(Res.drawable.ic_collections_bookmark_outlined),
					title = stringResource(Res.string.empty_library_title),
					message = stringResource(Res.string.empty_library_message)
				)
			}

			LibraryStatus.LOADED -> {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = contentPadding.only(vertical = true).add(bottom = 16.dp)
				) { 
					singleItem("scrollFix")
					
					items(
						items = lists,
						key = { it.first.id }
					) { (list, items) ->
						var showActionsDialog by remember { mutableStateOf<Pair<DBMedia, Media>?>(null) }

						showActionsDialog?.also { (dbMedia, media) ->
							MediaActionsDialog(
								extensionId = dbMedia.extensionId,
								media = media,
								onDismissRequest = { showActionsDialog = null }
							)
						}

						FeedRow(
							modifier = Modifier
								.fillMaxWidth()
								.thenIf(true) { /*clickable {*/
//									navigation.push(Routes.ExtensionFeed(
//										extensionId = extension.id,
//										extensionName = extension.name,
//										feedId = feed.id,
//										feedName = feed.name
//									))
								/*}*/ this }.animateItem(),

							contentPadding = contentPadding.only(horizontal = true)
								.add(horizontal = niceSideInset(), vertical = 8.dp),

							title = list.name,
							items = items.map { it.second },

							actions = {
//								IconButton(
//									modifier = Modifier
//										.size(16.dp)
//										.scale(scaleX = -2f, scaleY = 2f),
//									padding = 0.dp,
//									painter = painterResource(Res.drawable.ic_back),
//									contentDescription = null,
//									onClick = {
//										navigation.push(Routes.ExtensionFeed(
//											extensionId = extension.id,
//											extensionName = null,
//											feedId = feed.id,
//											feedName = feed.name
//										))
//									}
//								)
							},

							onMediaLongClick = { media ->
								showActionsDialog = items.first { it.second == media}.first to media
							},

							onMediaSelected = { media ->
								navigation.push(Routes.Media(
									extensionId = items.first { it.second == media }.first.extensionId,
									extensionName = null,
									media = media
								))
							}
						)
					}
				}
			}
		}
	}
}