package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.mrboomdev.awery.extension.loaders.getPoster
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.logo_aniyomi
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.resources.poster_no_image
import com.mrboomdev.awery.ui.popups.MediaActionsDialog
import com.mrboomdev.awery.ui.utils.contextMenuOpenDetector
import dev.chrisbanes.haze.HazeDefaults.style
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import org.jetbrains.compose.resources.painterResource

@Composable
fun MediaCard(
    modifier: Modifier = Modifier.width(115.dp),
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    media: Media
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .contextMenuOpenDetector {
                onLongClick?.invoke()
            }.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .fillMaxWidth()
                .aspectRatio(7F / 10F)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            
            model = media.getPoster()?.let { 
                ImageRequest.Builder(LocalPlatformContext.current)
                    .placeholderMemoryCacheKey(it)
                    .memoryCacheKey(it)
                    .data(it)
                    .build()
            },
            
            error = painterResource(Res.drawable.poster_no_image),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Text(
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            minLines = 2,
            maxLines = 2,
            text = media.title
        )
    }
}