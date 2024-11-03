package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.app.App.Companion.configuration
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ui.tv.components.FeaturedMediaCarousel
import com.mrboomdev.awery.ui.tv.components.MediaRow
import com.mrboomdev.awery.ui.tv.components.MediaRowContent

@Composable
fun FeedsScreen(
	sections: List<MediaRowContent>? = null,
	featuredItems: List<CatalogMedia>? = null,
	onItemSelected: (media: CatalogMedia) -> Unit
) {
	val listState = rememberLazyListState()

	LazyColumn (
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 32.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp),
		state = listState
	) {
		if(featuredItems != null) {
			item {
				FeaturedMediaCarousel(
					featuredContent = featuredItems,
					modifier = Modifier.height((configuration.screenHeightDp - 50).dp),
					onItemSelected = onItemSelected
				)
			}
		}

		if(sections != null) {
			items(sections) {
				MediaRow(
					content = it,
					onItemSelected = onItemSelected
				)
			}
		}
	}
}