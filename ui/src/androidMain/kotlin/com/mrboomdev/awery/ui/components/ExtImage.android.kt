package com.mrboomdev.awery.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.mrboomdev.awery.extension.sdk.Image

@Composable
actual fun ExtImage(
    modifier: Modifier,
    image: Image,
    contentScale: ContentScale
) {
    if(image.drawable != null) {
        return androidx.compose.foundation.Image(
            modifier = modifier,
            painter = rememberDrawablePainter(image.drawable),
            contentScale = contentScale,
            contentDescription = null
        )
    }

    AsyncImage(
        modifier = modifier,
        model = image.url ?: image.file ?: image.bytes,
        contentScale = contentScale,
        contentDescription = null
    )
}