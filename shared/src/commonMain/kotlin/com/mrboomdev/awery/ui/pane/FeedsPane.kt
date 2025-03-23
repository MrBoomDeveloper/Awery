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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.mrboomdev.awery.ui.utils.ScrollFixer
import com.mrboomdev.awery.ui.utils.only
import com.mrboomdev.awery.utils.UniqueIdGenerator
import com.mrboomdev.awery.utils.exceptions.explain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

sealed interface FeedsPaneLoadingState {
	data object Loading: FeedsPaneLoadingState
	data object End: FeedsPaneLoadingState
}

class FeedsPaneState(
	val loadedFeeds: SnapshotStateList<Pair<Long, LoadedFeed>> = mutableStateListOf(),
	private val coroutineScope: CoroutineScope
) {
	val loadingState = mutableStateOf<FeedsPaneLoadingState>(FeedsPaneLoadingState.Loading)
	internal val isRefreshing = mutableStateOf(false)
	internal var listState: LazyListState? = null
	private val idGenerator = UniqueIdGenerator.loopLong()
	private var job: Job? = null
	
	fun load(feeds: List<CatalogFeed>) {
		loadedFeeds.clear()
		
		job?.also {
			if(it.isActive) {
				it.cancel()
			}
		}
		
		job = coroutineScope.launch {
			val scrollFixer = ScrollFixer()
			
			feeds.processFeeds()
				.loadAll()
				.buffer(3)
				.onStart { loadingState.value = FeedsPaneLoadingState.Loading }
				.onEach { loadedFeed ->
					if(isRefreshing.value) isRefreshing.value = false
					loadedFeeds += idGenerator.long to loadedFeed
					listState?.also { scrollFixer.fix(it) }
				}.onCompletion {
					if(isRefreshing.value) isRefreshing.value = false
					loadingState.value = FeedsPaneLoadingState.End 
				}.collect()
		}
	}
}

/**
 * Please note that this composable does not load any data automatically.
 * You have to manually call [FeedsPaneState.load] to show any content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsPane(
	modifier: Modifier = Modifier,
	contentPadding: PaddingValues = PaddingValues(),
	state: FeedsPaneState,
	onSectionClick: (feed: CatalogFeed, results: CatalogSearchResults<CatalogMedia>) -> Unit,
	onMediaClick: (CatalogMedia) -> Unit,
	onReload: (() -> Unit)? = null
) {
	val listState = rememberLazyListState()
	
	DisposableEffect(state) {
		// Provide an list state to fix scrolling issues
		state.listState = listState
		
		onDispose { 
			// We don't want leaks :)
			state.listState = null
		}
	}
	
	val content = @Composable {
		LazyColumn(
			state = listState,
			contentPadding = contentPadding
		) {
			items(
				items = state.loadedFeeds,
				key = { it.first },
				contentType = {
					when {
						it.second.items != null -> 2
						it.second.throwable != null -> 1
						else -> 0
					}
				}
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
							ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
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
						.padding(64.dp)
						.animateItem(),
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					when(state.loadingState.value) {
						FeedsPaneLoadingState.Loading -> CircularProgressIndicator()
						
						FeedsPaneLoadingState.End -> {
							Text(
								fontWeight = FontWeight.SemiBold,
								textAlign = TextAlign.Center,
								style = MaterialTheme.typography.headlineMedium,
								text = stringResource(Res.string.you_reached_end)
							)
							
							Text(
								modifier = Modifier.padding(12.dp),
								textAlign = TextAlign.Center,
								text = stringResource(Res.string.you_reached_end_description)
							)
						}
					}
				}
			}
		}
	}
	
	if(onReload != null) {
		// We may handle reloading, so display an swipe-to-refresh box.
		PullToRefreshBox(
			modifier = modifier,
			isRefreshing = state.isRefreshing.value,
			onRefresh = {
				state.isRefreshing.value = true
				onReload()
			}
		) {
			content()
		}
	} else {
		// We doesn't handle reloading, so just show the list.
		content()
	}
}