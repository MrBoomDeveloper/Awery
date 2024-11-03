package com.mrboomdev.awery.ui.tv.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mrboomdev.awery.ext.data.CatalogMedia

data class MediaRowContent(
	val title: String,
	val items: List<CatalogMedia>
)

@Composable
fun MediaRow(
	content: MediaRowContent,
	modifier: Modifier = Modifier,
	onItemSelected: (CatalogMedia) -> Unit = {}
) {
	Row(
		modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 24.dp)
	) {
		Text(
			text = content.title,
			style = MaterialTheme.typography.headlineSmall,
			color = Color(0xFFFFFFFF)
		)
	}

	LazyRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(8.dp),
		contentPadding = PaddingValues(16.dp, 0.dp)
	) {
		items(content.items) { media ->
			MediaCard(
				media = media,
				onClick = { onItemSelected(media) }
			)
		}
	}
}