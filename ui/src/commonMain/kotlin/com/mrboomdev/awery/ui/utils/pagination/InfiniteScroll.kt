package com.mrboomdev.awery.ui.utils.pagination

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

@Composable
fun InfiniteScroll(
	state: LazyListState,
	buffer: Int = 1,
	loadMore: suspend () -> Unit
) {
	val shouldLoadMore = remember {
		derivedStateOf {
			val layoutInfo = state.layoutInfo
			val itemsCount = layoutInfo.totalItemsCount
			val lastVisibleIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
			itemsCount > 0 && lastVisibleIndex >= (itemsCount - buffer)
		}
	}

	LaunchedEffect(shouldLoadMore) {
		snapshotFlow { shouldLoadMore.value }
			.collect { shouldLoad ->
				if(shouldLoad) {
					loadMore()
				}
			}
	}
}

@Composable
fun InfiniteScroll(
	state: LazyGridState,
	buffer: Int = 2,
	loadMore: suspend () -> Unit
) {
	LaunchedEffect(state) {
		snapshotFlow { state.layoutInfo }
			.collect { layoutInfo ->
				val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
				val totalItems = layoutInfo.totalItemsCount

				if(totalItems > 0 && lastVisibleItem >= totalItems - buffer) {
					loadMore()
				}
			}
	}
}