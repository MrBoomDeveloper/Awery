package com.mrboomdev.awery.ui.routes

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.ui.components.ButtonWithIcon
import com.mrboomdev.awery.ui.components.ExpandableText
import com.mrboomdev.awery.ui.navigation.LocalNavHostController
import com.mrboomdev.awery.ui.utils.WINDOW_SIZE_MEDIUM
import com.mrboomdev.awery.ui.utils.compareTo
import com.mrboomdev.awery.ui.utils.plus
import com.mrboomdev.awery.utils.bannerOrPoster
import com.mrboomdev.awery.utils.enumStateSaver
import com.mrboomdev.awery.utils.posterOrBanner
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

enum class MediaRouteTab {
	INFO,
	PLAY,
	COMMENTS,
	RELATIONS
}

@Serializable
data class MediaRoute(
	val media: CatalogMedia,
	val initialTab: MediaRouteTab = MediaRouteTab.INFO
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaRoute.Companion.Content(
	args: MediaRoute,
	viewModel: MediaRouteViewModel = viewModel()
) {
	val navigation = LocalNavHostController.current
	val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
	val currentTab = rememberSaveable(saver = enumStateSaver()) { mutableStateOf(args.initialTab) }
	var didLoadBackgroundImage by remember { mutableStateOf(false) }
	val backgroundColor by animateColorAsState(if(didLoadBackgroundImage) Color.Black else Color.Transparent)
	
	val backgroundOverlayColor by animateColorAsState(if(didLoadBackgroundImage) {
		MaterialTheme.colorScheme.background.copy(alpha = .85f)
	} else MaterialTheme.colorScheme.background)
	
	if(windowSizeClass >= WINDOW_SIZE_MEDIUM) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(backgroundColor)
		) {
			AsyncImage(
				modifier = Modifier.fillMaxSize(),
				model = args.media.bannerOrPoster,
				contentScale = ContentScale.Crop,
				contentDescription = null,
				onSuccess = { didLoadBackgroundImage = true }
			)
			
			Image(
				modifier = Modifier.fillMaxSize(),
				painter = ColorPainter(backgroundOverlayColor),
				contentDescription = null
			)
			
			Row(modifier = Modifier.fillMaxSize()) {
				Column(
					modifier = Modifier
						.padding(WindowInsets.safeContent.only(
							WindowInsetsSides.Top + WindowInsetsSides.Left + WindowInsetsSides.Bottom
						).asPaddingValues())
						.padding(16.dp),
					verticalArrangement = Arrangement.spacedBy(4.dp)
				) {
					SmallFloatingActionButton(
						containerColor = MaterialTheme.colorScheme.surfaceContainer,
						onClick = { navigation.popBackStack() }
					) {
						Icon(
							modifier = Modifier.size(24.dp),
							painter = painterResource(Res.drawable.ic_back),
							contentDescription = stringResource(Res.string.back)
						)
					}
					
					args.media.extras[CatalogMedia.EXTRA_SHARE]?.also { url ->
						SmallFloatingActionButton(
							containerColor = MaterialTheme.colorScheme.surfaceContainer,
							onClick = { Platform.share(url) }
						) {
							Icon(
								modifier = Modifier.size(24.dp),
								painter = painterResource(Res.drawable.ic_share_filled),
								contentDescription = stringResource(Res.string.share)
							)
						}
					}
				}
				
				args.media.posterOrBanner?.let {
					Column(
						modifier = Modifier.fillMaxHeight()
							.padding(WindowInsets.safeContent.only(
								WindowInsetsSides.Top + WindowInsetsSides.Bottom
							).asPaddingValues())
							.padding(start = 8.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
					) {
						AsyncImage(
							modifier = Modifier
								.fillMaxHeight()
								.aspectRatio(9 / 14f)
								.clip(RoundedCornerShape(16.dp)),
							model = it,
							contentScale = ContentScale.Crop,
							contentDescription = null
						)
					}
				}
				
				Spacer(Modifier.width(8.dp))
				
				LazyColumn(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f),
					contentPadding = WindowInsets.safeContent.only(
						WindowInsetsSides.Top + WindowInsetsSides.Right + WindowInsetsSides.Bottom
					).asPaddingValues() + PaddingValues(top = 16.dp, bottom = 16.dp)
				) {
					MediaRouteContent(
						args = args,
						currentTab = currentTab
					)
				}
			}
		}
	} else {
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
		) {
			if(currentTab.value != MediaRouteTab.INFO) {
				item("compactHero") {
					Row(
						modifier = Modifier
							.fillMaxSize()
							.padding(WindowInsets.safeContent.only(
								WindowInsetsSides.Top + WindowInsetsSides.Horizontal
							).asPaddingValues())
							.padding(horizontal = 8.dp)
							.animateItem(),
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						SmallFloatingActionButton(
							containerColor = MaterialTheme.colorScheme.surfaceContainer,
							onClick = { navigation.popBackStack() }
						) {
							Icon(
								modifier = Modifier.size(24.dp),
								painter = painterResource(Res.drawable.ic_back),
								contentDescription = stringResource(Res.string.back)
							)
						}
						
						Spacer(Modifier.weight(1f))
						
						args.media.extras[CatalogMedia.EXTRA_SHARE]?.also { url ->
							SmallFloatingActionButton(
								containerColor = MaterialTheme.colorScheme.surfaceContainer,
								onClick = { Platform.share(url) }
							) {
								Icon(
									modifier = Modifier.size(24.dp),
									painter = painterResource(Res.drawable.ic_share_filled),
									contentDescription = stringResource(Res.string.share)
								)
							}
						} ?: run {
							Spacer(Modifier.width(40.dp))
						}
					}
				}
			} else {
				item("hero") {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.animateItem()
					) {
						AsyncImage(
							modifier = Modifier
								.matchParentSize()
								.alpha(.15f),
							model = args.media.bannerOrPoster,
							contentScale = ContentScale.Crop,
							contentDescription = null,
							onSuccess = { didLoadBackgroundImage = true }
						)
						
						val gradient = Brush.verticalGradient(listOf(
							MaterialTheme.colorScheme.background.copy(alpha = 0f),
							MaterialTheme.colorScheme.background
						))
						
						Canvas(
							modifier = Modifier
								.fillMaxWidth()
								.height(160.dp)
								.align(Alignment.BottomCenter)
						) {
							drawRect(gradient)
						}
						
						Row(
							modifier = Modifier
								.fillMaxSize()
								.padding(WindowInsets.safeContent.only(
									WindowInsetsSides.Top + WindowInsetsSides.Horizontal
								).asPaddingValues())
								.padding(horizontal = 8.dp),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							SmallFloatingActionButton(
								containerColor = MaterialTheme.colorScheme.surfaceContainer,
								onClick = { navigation.popBackStack() }
							) {
								Icon(
									modifier = Modifier.size(24.dp),
									painter = painterResource(Res.drawable.ic_back),
									contentDescription = stringResource(Res.string.back)
								)
							}
							
							Spacer(Modifier.width(8.dp))
							
							Column(
								modifier = Modifier
									.weight(1f)
									.fillMaxHeight()
									.padding(vertical = 16.dp)
							) {
								AsyncImage(
									modifier = Modifier
										.fillMaxWidth()
										.aspectRatio(9f / 14f)
										.heightIn(max = 400.dp)
										.clip(RoundedCornerShape(16.dp)),
									model = args.media.posterOrBanner,
									contentScale = ContentScale.Crop,
									contentDescription = null
								)
							}
							
							Spacer(Modifier.width(8.dp))
							
							args.media.extras[CatalogMedia.EXTRA_SHARE]?.also { url ->
								SmallFloatingActionButton(
									containerColor = MaterialTheme.colorScheme.surfaceContainer,
									onClick = { Platform.share(url) }
								) {
									Icon(
										modifier = Modifier.size(24.dp),
										painter = painterResource(Res.drawable.ic_share_filled),
										contentDescription = stringResource(Res.string.share)
									)
								}
							} ?: run {
								Spacer(Modifier.width(40.dp))
							}
						}
					}
					
					Spacer(Modifier.height(16.dp))
				}
			}
			
			MediaRouteContent(
				args = args,
				currentTab = currentTab
			)
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Suppress("FunctionName")
private fun LazyListScope.MediaRouteContent(
	args: MediaRoute,
	currentTab: MutableState<MediaRouteTab>
) {
	if(currentTab.value == MediaRouteTab.INFO) {
		item("main_meta") {
			Column(
				modifier = Modifier
					.animateItem()
			) {
				SelectionContainer {
					Text(
						modifier = Modifier.padding(horizontal = 16.dp),
						style = MaterialTheme.typography.headlineLarge,
						fontWeight = FontWeight.SemiBold,
						text = args.media.title
					)
				}
				
				Spacer(Modifier.height(8.dp))
				
				buildList {
					args.media.extras.also { extras ->
						extras[CatalogMedia.EXTRA_COUNTRY]?.also { add(it) }
						extras[CatalogMedia.EXTRA_AGE_RATING]?.also { add(it) }
					}
				}.also {
					if(it.isNotEmpty()) {
						Text(it.joinToString(" • "))
					}
				}
				
				Spacer(Modifier.height(8.dp))
				
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.horizontalScroll(rememberScrollState()),
					horizontalArrangement = Arrangement.spacedBy(2.dp)
				) {
					Spacer(Modifier.width(16.dp))
					
					ButtonWithIcon(
						iconSize = 26.dp,
						icon = painterResource(Res.drawable.ic_play_filled),
						text = stringResource(Res.string.watch_now),
						onClick = {
							currentTab.value = MediaRouteTab.PLAY
						}
					)
					
					FilledIconButton(
						modifier = Modifier
							.fillMaxHeight()
							.aspectRatio(1f),
						onClick = {}
					) {
						Icon(
							modifier = Modifier.padding(8.dp),
							painter = painterResource(Res.drawable.ic_bookmark_filled),
							contentDescription = null
						)
					}
					
					FilledIconButton(
						modifier = Modifier
							.fillMaxHeight()
							.aspectRatio(1f),
						onClick = {}
					) {
						Icon(
							painter = painterResource(Res.drawable.ic_sync),
							contentDescription = null
						)
					}
					
					args.media.extras[CatalogMedia.EXTRA_SHARE]?.also { url ->
						FilledIconButton(
							modifier = Modifier
								.fillMaxHeight()
								.aspectRatio(1f),
							onClick = { Platform.openUrl(url) }
						) {
							Icon(
								modifier = Modifier.padding(8.dp),
								painter = painterResource(Res.drawable.ic_language),
								contentDescription = null
							)
						}
					}
					
					Spacer(Modifier.width(16.dp))
				}
			}
		}
	}
			
	item {
		Column {
			Spacer(Modifier.height(6.dp))
			
			SecondaryScrollableTabRow(
				modifier = Modifier.fillMaxWidth(),
				edgePadding = 16.dp,
				selectedTabIndex = currentTab.value.ordinal,
				containerColor = Color.Transparent
			) {
				MediaRouteTab.entries.forEach { tab ->
					Tab(
						selected = currentTab.value == tab,
						onClick = { currentTab.value = tab }
					) {
						Text(
							modifier = Modifier.padding(
								horizontal = 18.dp,
								vertical = 12.dp
							),
							fontSize = 14.sp,
							text = when(tab) {
								MediaRouteTab.INFO -> stringResource(Res.string.info)
								MediaRouteTab.PLAY -> stringResource(Res.string.episodes)
								MediaRouteTab.COMMENTS -> stringResource(Res.string.comments)
								MediaRouteTab.RELATIONS -> stringResource(Res.string.relations)
							}
						)
					}
				}
			}
		}
	}
	
	when(currentTab.value) {
		MediaRouteTab.INFO -> item("info") {
			args.media.extras[CatalogMedia.EXTRA_DESCRIPTION]?.also { description ->
				ExpandableText(
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.animateItem(),
					maxLines = 5,
					text = description,
					isSelectable = true
				) { state ->
					TextButton(
						modifier = Modifier.fillMaxWidth(),
						onClick = state::toggle
					) {
						Text(if(state.isExpanded) "Read less" else "Read more")
					}
				}
			}
		}
		
		MediaRouteTab.PLAY -> {
			
		}
		
		MediaRouteTab.COMMENTS -> {
			
		}
		
		MediaRouteTab.RELATIONS -> {
			
		}
	}
}

class MediaRouteViewModel(savedStateHandle: SavedStateHandle): ViewModel() {
	
}