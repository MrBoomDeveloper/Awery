package com.mrboomdev.awery.ui.pane

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogSearchResults
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.sources.LoadedFeed
import com.mrboomdev.awery.sources.loadAll
import com.mrboomdev.awery.sources.processFeeds
import com.mrboomdev.awery.ui.components.SmallCard
import com.mrboomdev.awery.ui.utils.only
import com.mrboomdev.awery.utils.UniqueIdGenerator
import com.mrboomdev.awery.utils.exceptions.explain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private sealed interface State {
	object Loading: State
	object End: State
	data class Failed(val throwable: Throwable): State
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogPane(
	modifier: Modifier = Modifier,
	contentPadding: PaddingValues = PaddingValues(),
	feeds: List<CatalogFeed>,
	onSectionClick: (feed: CatalogFeed, results: CatalogSearchResults<CatalogMedia>) -> Unit,
	onMediaClick: (CatalogMedia) -> Unit
) {
	val loadedFeeds = remember(feeds) { mutableStateListOf<Pair<Long, LoadedFeed>>() }
	val idGenerator = remember(feeds) { UniqueIdGenerator.loopLong() }
	val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
	var isRefreshing by remember(feeds) { mutableStateOf(false) }
	var state by remember { mutableStateOf<State>(State.Loading) }
	
	suspend fun load() {
		state = State.Loading
		feeds.processFeeds()
			.loadAll()
			.buffer(3)
			.collect { loadedFeeds += idGenerator.long to it }
	}
	
	LaunchedEffect(feeds) {
		coroutineScope.launch { 
			load()
		}
	}
	
	PullToRefreshBox(
		modifier = modifier,
		isRefreshing = isRefreshing,
		onRefresh = {
			isRefreshing = true
			
			if(coroutineScope.isActive) {
				coroutineScope.cancel()
			}
			
			coroutineScope.launch { 
				load()
			}
		}
	) {
		LazyColumn(
			contentPadding = contentPadding
		) { 
			items(
				items = loadedFeeds,
				key = { it.first },
				contentType = { when {
					it.second.items != null -> 2
					it.second.throwable != null -> 1
					else -> 0
				} }
			) { (_, feed) ->
				Row(
					modifier = Modifier
						.padding(contentPadding.only(top = false, bottom = false))
						.padding(vertical = 8.dp),
					verticalAlignment = Alignment.CenterVertically
				) { 
					Text(
						style = MaterialTheme.typography.titleLarge,
						fontWeight = FontWeight.SemiBold,
						text = feed.feed.title
					)
					
					Spacer(Modifier.width(16.dp))
				}
				
				if(feed.throwable != null) {
					SelectionContainer {
						Text(
							modifier = Modifier
								.padding(contentPadding.only(top = false, bottom = false)),
							style = MaterialTheme.typography.bodyMedium,
							text = feed.throwable.explain().print(),
							lineHeight = 20.sp,
							maxLines = 5
						)
					}
					
					Spacer(Modifier.height(8.dp))
				}
				
				if(feed.items != null) {
					Spacer(Modifier.height(8.dp))
					
					LazyRow(
						modifier = Modifier.animateItem(),
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						contentPadding = contentPadding.only(top = false, bottom = false)
					) {
						items(
							items = feed.items,
							key = { it.globalId.value }
						) { media ->
							CompositionLocalProvider(
								LocalTextStyle provides MaterialTheme.typography.bodyMedium
							) {
								SmallCard(
									image = media.extras[CatalogMedia.EXTRA_POSTER],
									title = media.title,
									onClick = { onMediaClick(media) }
								)
							}
						}
					}
				}
				
				Spacer(Modifier.height(8.dp))
			}
			
			item("state") {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(36.dp)
						.animateItem(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					when(val mState = state) {
						State.Loading -> CircularProgressIndicator()
						
						State.End -> {
							Text(
								style = MaterialTheme.typography.headlineSmall,
								text = stringResource(Res.string.you_reached_end)
							)
								
							Text(
								modifier = Modifier.padding(8.dp),
								textAlign = TextAlign.Center,
								text = stringResource(Res.string.you_reached_end_description)
							)
						}
						
						is State.Failed -> {
							val explained = remember(state) { mState.throwable.explain() }
							
							Text(
								style = MaterialTheme.typography.headlineSmall,
								text = explained.title
							)
							
							Text(
								modifier = Modifier.padding(8.dp),
								textAlign = TextAlign.Center,
								text = explained.message
							)
						}
					}
				}
			}
		}
	}
}