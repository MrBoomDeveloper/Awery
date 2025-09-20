package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.mrboomdev.awery.extension.sdk.Image

@Composable
actual fun ExtImage(
    modifier: Modifier,
    image: Image,
    contentScale: ContentScale
) {
    AsyncImage(
        modifier = modifier,
        model = image.url ?: image.file ?: image.bytes,
        contentScale = contentScale,
        contentDescription = null
    )
}