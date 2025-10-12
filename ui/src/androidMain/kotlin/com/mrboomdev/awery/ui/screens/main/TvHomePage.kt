package com.mrboomdev.awery.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.screens.home.HomeViewModel
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.thenIf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource

@Composable
internal fun TvHomePage(
	viewModel: MainScreenViewModel,
	contentPadding: PaddingValues
) {
	val navigation = Navigation.current()
	val viewModel = viewModel { HomeViewModel() }
	val isLoading by viewModel.isLoading.collectAsState()
	
	LazyColumn(
		contentPadding = contentPadding
	) { 
		singleItem("scrollFixer")
		
		items(
			items = viewModel.loadedFeeds,
			key = { it.first.id + it.second.id }
		) { (extension, feed, mediaResults) ->
			Column(Modifier.animateItem()) {
				val firstItemFocusRequester = remember { FocusRequester() }
				
				Text(
					modifier = Modifier.padding(16.dp),
					style = MaterialTheme.typography.titleLarge,
					color = MaterialTheme.colorScheme.onSurface, 
					text = "${feed.name} - ${extension.name}"
				)
				
				LazyRow(
//					modifier = Modifier.focusRestorer(firstItemFocusRequester),
					contentPadding = PaddingValues(horizontal = 16.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					itemsIndexed(
						items = mediaResults.items,
						key = { _, media -> media.id },
						contentType = { _, _ -> "media" }
					) { index, media ->
						StandardCardContainer(
							modifier = Modifier
								.width(115.dp)
								.thenIf(index == 0) { focusRequester(firstItemFocusRequester) },
							
							imageCard = { interactionSource ->
								Card(
									interactionSource = interactionSource,
									onClick = {
										navigation.push(Routes.Media(
											extensionId = extension.id,
											extensionName = extension.name,
											media = media
										))
									}
								) {
									AsyncImage(
										modifier = Modifier
											.fillMaxWidth()
											.aspectRatio(7F / 10F)
											.background(MaterialTheme.colorScheme.surfaceVariant),

										model = media.getPoster()?.let {
											ImageRequest.Builder(LocalPlatformContext.current)
												.placeholderMemoryCacheKey(it)
												.memoryCacheKey(it)
												.data(it)
												.build()
										},

										error = painterResource(Res.drawable.poster_no_image),
										contentScale = ContentScale.Crop,
										contentDescription = null
									)
								}
							},
							
							title = { 
								Text(
									modifier = Modifier.padding(top = 8.dp),
									textAlign = TextAlign.Center,
									maxLines = 2,
									text = media.title
								) 
							},
							
							subtitle = {
								media.episodes?.also { episodes ->
									Text(
										modifier = Modifier.padding(top = 4.dp),
										text = pluralStringResource(when(media.type) {
											Media.Type.WATCHABLE -> Res.plurals.episodes
											Media.Type.READABLE -> Res.plurals.chapters
										}, episodes, episodes)
									)
								}
							}
						)
					}
					
					if(mediaResults.hasNextPage) {
						singleItem("more") {
							Card(
								modifier = Modifier
									.width(115.dp)
									.aspectRatio(7F / 10F),
								
								onClick = {
									navigation.push(Routes.ExtensionFeed(
										extensionId = extension.id,
										extensionName = extension.name,
										feedId = feed.id,
										feedName = feed.name
									))
								}
							) {
								Icon(
									modifier = Modifier
										.background(Color(0x11ffffff))
										.fillMaxSize()
										.wrapContentSize(Alignment.Center)
										.size(48.dp)
										.scale(scaleX = -1f, scaleY = 1f),
									painter = painterResource(Res.drawable.ic_back),
									contentDescription = null
								)
							}
						}
					}
				}
			}
		}
		
		if(isLoading) {
			singleItem("loadingIndicator") {
				CircularProgressIndicator(
					modifier = Modifier
						.fillMaxWidth()
						.wrapContentSize(Alignment.Center)
						.padding(32.dp)
						.animateItem()
				)
			}
		}
	}
}