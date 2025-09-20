package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mrboomdev.awery.extension.sdk.Image

/**
 * Just like [AsyncImage], but allows [Image] rendering!
 */
@Composable
expect fun ExtImage(
    modifier: Modifier = Modifier,
    image: Image,
    contentScale: ContentScale = ContentScale.Fit
)

@Composable
fun DefaultExtImage(
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            autoSize = TextAutoSize.StepBased(minFontSize = 2.sp),
            text = "📦"
        )
    }
}