package com.mrboomdev.awery.ui.screens.search

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.core.utils.collection.limit
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.navigation.Routes
import com.mrboomdev.awery.ui.components.*
import com.mrboomdev.awery.ui.navigation.RouteInfo
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.navigation.core.plusAssign
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private enum class SearchStatus {
	LOADING,
	LOADED,
	EMPTY
}

private const val MAX_COLLAPSED_EXTENSIONS = 3

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
	viewModel: SearchViewModel = viewModel { SearchViewModel() },
	contentPadding: PaddingValues
) {
	val navigation = Navigation.current()
	val query by App.searchQuery.collectAsState()
	val isLoadingFeeds by viewModel.isLoadingFeeds.collectAsState()
	val isLoadingExtensions by Extensions.observeIsLoading().collectAsState()

	val extensions by remember(query) {
		viewModel.extensionsFound.map { extensions ->
			if(query.isNotBlank()) {
				extensions?.limit(MAX_COLLAPSED_EXTENSIONS)
			} else extensions
		}
	}.collectAsState(viewModel.extensionsFound.value)
	
	val inlineContent = remember {
		mapOf(
			"nsfw" to InlineTextContent(
				Placeholder(
					width = 14.sp,
					height = 14.sp,
					placeholderVerticalAlign = PlaceholderVerticalAlign.Center
				)
			) {
				Icon(
					painter = painterResource(Res.drawable.ic_explict_outlined),
					contentDescription = stringResource(Res.string.adult_content)
				)
			}
		)
	}

	Crossfade(when {
		(extensions?.isNotEmpty() ?: false) 
				|| viewModel.loadedFeeds.isNotEmpty() 
				|| (!isLoadingFeeds && viewModel.failedFeeds.isNotEmpty()) -> SearchStatus.LOADED
		
		!isLoadingExtensions 
				&& (extensions?.isEmpty() ?: false) 
				&& !isLoadingFeeds -> SearchStatus.EMPTY
		
		else -> SearchStatus.LOADING
	}) { status ->
		when(status) {
			SearchStatus.LOADING -> {
				LoadingIndicator(
					modifier = Modifier
						.fillMaxSize()
						.wrapContentSize(Alignment.Center)
				)
			}
			
			SearchStatus.EMPTY -> {
				InfoBox(
					modifier = Modifier
						.fillMaxSize()
						.wrapContentSize(Alignment.Center),
					icon = painterResource(Res.drawable.ic_search),
					title = "Nothing found",
					message = "No results were found matching your query. Check if you've entered everything correctly."
				)
			}
			
			SearchStatus.LOADED -> {
				LazyColumn(
					modifier = Modifier.fillMaxSize(),
					contentPadding = contentPadding
				) {
					singleItem("scrollFix")

					safeItems(
						items = extensions ?: emptyList(),
						key = { "extension_${it.id}"},
						contentType = { "extension" }
					) { extension ->
						val subtitle = remember(extension) {
							buildAnnotatedString {
								if(extension.isNsfw) {
									appendInlineContent("nsfw")
									append(" NSFW ")
								}
								
								append(listOfNotNull(
									extension.lang?.formatAsLanguage(),
									extension.id.takeIf { AwerySettings.showIds.value }
								).also {
									if(it.isNotEmpty() && extension.isNsfw) {
										append(" • ")
									}
								}.joinToString(" • "))
							}.takeIf { it.text.isNotBlank() }
						}
						
						Row(
							modifier = Modifier
								.clickable { navigation.push(Routes.Extension(extension.id, extension.name)) }
								.fillMaxWidth()
								.padding(vertical = 8.dp, horizontal = niceSideInset())
								.heightIn(min = 48.dp)
								.animateItem(),
							horizontalArrangement = Arrangement.spacedBy(12.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							extension.icon?.also { icon ->
								ExtImage(Modifier.size(36.dp), icon)
							} ?: run {
								DefaultExtImage(Modifier.size(36.dp))
							}

							Column(
								verticalArrangement = Arrangement.spacedBy(2.dp)
							) {
								Text(extension.name)

								subtitle?.also {
									Text(
										style = MaterialTheme.typography.bodySmall,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
										inlineContent = inlineContent,
										text = it
									)
								}
							}
						}
					}
					
					safeItems(
						items = viewModel.loadedFeeds,
						key = { "loaded_feed_${it.first.id}" },
						contentType = { "loadedFeed" }
					) { (extension, results, filters) ->
						var showActionsDialog by remember { mutableStateOf<Media?>(null) }

						showActionsDialog?.also { media ->
							MediaActionsDialog(
								extensionId = extension.id,
								media = media,
								onDismissRequest = { showActionsDialog = null }
							)
						}
						
						FeedRow(
							modifier = Modifier
								.fillMaxWidth()
								.thenIf(results.hasNextPage && Awery.platform != Platform.DESKTOP) { clickable {
									navigation.push(Routes.ExtensionSearch(
										extensionId = extension.id,
										extensionName = extension.name,
										filters = filters
									))
								} }.animateItem(),
							contentPadding = PaddingValues(vertical = 16.dp, horizontal = niceSideInset()),
							title = extension.name,
							items = results.items,

							actions = {
								if(results.hasNextPage) {
									IconButton(
										modifier = Modifier
											.size(16.dp)
											.scale(scaleX = -2f, scaleY = 2f),
										padding = 0.dp,
										painter = painterResource(Res.drawable.ic_back),
										contentDescription = null,
										onClick = {
											navigation.push(Routes.ExtensionSearch(
												extensionId = extension.id,
												extensionName = extension.name,
												filters = filters
											))
										}
									)
								}
							},
							
							onMediaSelected = { media ->
								navigation += Routes.Media(
									extensionId = extension.id,
									extensionName = extension.name,
									media = media
								)
							},

							onMediaLongClick = { media ->
								showActionsDialog = media
							}
						)
					}

					if(isLoadingExtensions || isLoadingFeeds) {
						singleItem("loading") {
							LoadingIndicator(
								modifier = Modifier
									.fillMaxWidth()
									.padding(64.dp)
									.wrapContentSize(Alignment.Center)
									.animateItem()
							)
						}
					}

					safeItems(
						items = viewModel.failedFeeds,
						key = { "failed_feed_${it.first.id}" },
						contentType = { "failedFeed" }
					) { (extension, exception, filters) ->
						var isReloading by remember(extension, exception) { mutableStateOf(false) }
						
						FeedRow(
							modifier = Modifier
								.fillMaxWidth()
								.animateItem(),
							contentPadding = PaddingValues(vertical = 16.dp, horizontal = niceSideInset()),
							title = extension.name,
							actions = {
								if(isReloading) return@FeedRow

								extension.webpage?.also { webpage ->
									IconButton(
										modifier = Modifier.size(42.dp),
										padding = 9.dp,
										painter = painterResource(Res.drawable.ic_language),
										contentDescription = null,
										onClick = { navigation.push(Routes.Browser(webpage)) }
									)
								}

								IconButton(
									modifier = Modifier.size(42.dp),
									painter = painterResource(Res.drawable.ic_refresh),
									contentDescription = null,
									onClick = {
										isReloading = true

										viewModel.reloadFeed(extension, filters) {
											isReloading = false
										}
									}
								)
							}
						) { contentPadding ->
							Crossfade(isReloading) { isReloading ->
								if(isReloading) {
									LinearProgressIndicator(
										modifier = Modifier
											.fillMaxWidth()
											.padding(contentPadding)
											.padding(top = 32.dp, bottom = 16.dp)
									)

									return@Crossfade
								}

								SelectionContainer {
									var expand by remember { mutableStateOf(false) }

									ExpandableText(
										modifier = Modifier
											.padding(contentPadding)
											.padding(top = 6.dp),
										isExpanded = expand,
										onExpand = { expand = it },
										maxLines = 5,
										text = exception.classify().message
									)
								}
							}
						}
					}
				}
			}
		}
	}
}