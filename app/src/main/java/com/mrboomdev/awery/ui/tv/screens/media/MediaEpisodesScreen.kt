package com.mrboomdev.awery.ui.tv.screens.media

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.ext.data.CatalogMedia

@Composable
fun MediaEpisodesScreen(media: CatalogMedia) {
	Box(modifier = Modifier.fillMaxSize()) {
		LazyHorizontalGrid(rows = GridCells.Adaptive(100.dp)) {
			item(span = { GridItemSpan(maxLineSpan) }) {

			}
		}
	}
}

@Composable
@Preview(
	showBackground = true,
	device = "id:tv_720p"
)
fun MediaEpisodesScreenPreview() {

}