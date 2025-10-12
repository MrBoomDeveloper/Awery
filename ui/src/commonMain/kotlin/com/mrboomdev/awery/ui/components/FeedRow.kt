package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.ui.utils.contextMenuOpenDetector
import com.mrboomdev.awery.ui.utils.exclude

@Composable
fun FeedRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    items: List<Media>,
    onMediaSelected: (Media) -> Unit,
    onMediaLongClick: ((Media) -> Unit)? = null
) {
    FeedRow(
        modifier = modifier,
        contentPadding = contentPadding,
        title = title,
        actions = actions
    ) { paddingValues ->
        Spacer(Modifier.height(16.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = items,
                key = { it.id }
            ) { media ->
                MediaCard(
                    modifier = Modifier
						.contextMenuOpenDetector {
							onMediaLongClick?.let {{ it(media) }}
						}.width(115.dp)
                        .animateItem(),
                    media = media,
                    onClick = { onMediaSelected(media) },
                    onLongClick = onMediaLongClick?.let {{ it(media) }}
                )
            }
        }
    }
}

@Composable
fun FeedRow(
    modifier: Modifier = Modifier,
    title: String,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.(PaddingValues) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding.exclude(bottom = true)),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Normal,
                text = title
            )
            
            Row {
                actions()
            }
        }

        content(contentPadding.exclude(top = true))
    }
}