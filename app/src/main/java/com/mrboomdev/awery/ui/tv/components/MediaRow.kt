package com.mrboomdev.awery.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.util.exceptions.explain

@Composable
fun MediaRow(
	content: CatalogFeed.Loaded,
	modifier: Modifier = Modifier,
	onItemSelected: (CatalogMedia) -> Unit = {}
) {
	Row(
		modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 24.dp)
	) {
		Text(
			text = content.feed.title,
			style = MaterialTheme.typography.headlineSmall,
			color = Color(0xFFFFFFFF)
		)
	}

	if(content.items != null) {
		LazyRow(
			modifier = modifier,
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			contentPadding = PaddingValues(16.dp, 0.dp)
		) {
			items(content.items as List<CatalogMedia>) { media ->
				MediaCard(
					media = media,
					onClick = { onItemSelected(media) },
					modifier = Modifier
						.width(125.dp)
						.height(240.dp)
				)
			}
		}
	} else {
		if(content.throwable != null) {
			Text(
				modifier = Modifier.padding(16.dp, 0.dp),
				text = content.throwable!!.explain().print(),
				style = MaterialTheme.typography.bodyLarge,
				color = Color(0xFFCACACA),
				maxLines = 5
			)
		}
	}
}