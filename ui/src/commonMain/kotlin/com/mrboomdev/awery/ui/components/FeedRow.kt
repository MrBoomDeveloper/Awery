package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.AweryFonts
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
            modifier = modifier,
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = items,
                key = { it.id }
            ) { media ->
                MediaCard(
                    modifier = Modifier
                        .width(115.dp)
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
                text = title
            )
            
            Row {
                actions()
            }
        }

        content(contentPadding.exclude(top = true))
    }
}