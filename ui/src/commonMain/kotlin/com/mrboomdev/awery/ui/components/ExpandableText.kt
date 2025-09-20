package com.mrboomdev.awery.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.next
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.read_less
import com.mrboomdev.awery.resources.read_more
import dev.chrisbanes.haze.HazeDefaults.style
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonNull.content
import org.jetbrains.compose.resources.stringResource
import java.awt.SystemColor.text

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    buttonHeight: Dp = 48.dp,
    maxLines: Int,
    isExpanded: Boolean,
    onExpand: (Boolean) -> Unit,
    text: String
) {
    ExpandableTextImpl(
        modifier = modifier,
        buttonHeight = buttonHeight,
        maxLines = maxLines,
        isExpanded = isExpanded,
        onExpand = onExpand,
        inlineContent = null,
        text = text
    )
}

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    buttonHeight: Dp = 48.dp,
    maxLines: Int,
    isExpanded: Boolean,
    onExpand: (Boolean) -> Unit,
    inlineContent: Map<String, InlineTextContent> = mapOf(),
    text: AnnotatedString
) {
    ExpandableTextImpl(
        modifier = modifier,
        buttonHeight = buttonHeight,
        maxLines = maxLines,
        isExpanded = isExpanded,
        onExpand = onExpand,
        inlineContent = inlineContent,
        text = text
    )
}

@Composable
private fun ExpandableTextImpl(
    modifier: Modifier,
    buttonHeight: Dp,
    maxLines: Int,
    isExpanded: Boolean,
    onExpand: (Boolean) -> Unit,
    inlineContent: Map<String, InlineTextContent>?,
    text: CharSequence
) {
    var doesOverflow by remember { mutableStateOf<Boolean?>(null) }
    
    val gradientBottom = doesOverflow?.let { 
        animateColorAsState(if(it) {
            Color.Transparent
        } else Color.Black).value
    } ?: Color.Transparent

    Column(
        modifier = modifier
    ) {
        Box {
            // Visible text.
            // We duplicate text so that is would have smooth collapse animation,
            // because maxLines property is not animatable.
            if(doesOverflow != null) {
                val modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithCache {
                        val gradient = Brush.verticalGradient(listOf(
                            if(doesOverflow == null) {
                                Color.Transparent
                            } else Color.Black, 
                            gradientBottom
                        ))
                        
                        onDrawWithContent { 
                            drawContent()
                            drawRect(
                                brush = gradient,
                                blendMode = BlendMode.DstIn,
                                topLeft = Offset(0f, size.height - buttonHeight.toPx()),
                                size = Size(size.width, buttonHeight.toPx())
                            )
                        }
                    }
                
                if(text is AnnotatedString) {
                    Text(
                        modifier = modifier,
                        overflow = TextOverflow.Ellipsis,
                        inlineContent = inlineContent!!,
                        text = text
                    )
                } else {
                    Text(
                        modifier = modifier,
                        overflow = TextOverflow.Ellipsis,
                        text = text.toString()
                    )
                }
            }

            // Hidden text for measurements
            Column(Modifier.animateContentSize().alpha(0f)) {
                if(text is AnnotatedString) {
                    Text(
                        overflow = TextOverflow.Ellipsis,
                        inlineContent = inlineContent!!,
                        text = text,
                        maxLines = if(isExpanded) Int.MAX_VALUE else maxLines,
                        onTextLayout = { doesOverflow = it.didOverflowHeight }
                    )
                } else {
                    Text(
                        overflow = TextOverflow.Ellipsis,
                        text = text.toString(),
                        maxLines = if(isExpanded) Int.MAX_VALUE else maxLines,
                        onTextLayout = { doesOverflow = it.didOverflowHeight }
                    )
                }

                Spacer(Modifier.height(if(isExpanded) {
                    buttonHeight
                } else 0.dp))
            }

            if(doesOverflow == true || isExpanded) {
                DisableSelection {
                    TextButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .align(Alignment.BottomCenter),
                        onClick = { onExpand(!isExpanded) }
                    ) {
                        Text(
                            text = stringResource(if(isExpanded) {
                                Res.string.read_less
                            } else Res.string.read_more) + "..."
                        )
                    }
                }
            }
        }
    }
}