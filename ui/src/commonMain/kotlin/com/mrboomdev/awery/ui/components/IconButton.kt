package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource

@Composable
fun IconButton(
    modifier: Modifier = Modifier.size(48.dp),
    padding: Dp = 8.dp,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String?
) {
    IconButton(
        modifier = modifier,
        padding = padding,
        enabled = enabled,
        interactionSource = interactionSource,
        colors = colors,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = contentDescription
        )
    }
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier.size(48.dp),
    padding: Dp = 8.dp,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = colors.containerColor(enabled))
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        val contentColor = colors.contentColor(enabled)
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

@Stable
private fun IconButtonColors.contentColor(enabled: Boolean) =
    if(enabled) contentColor else disabledContentColor

@Stable
private fun IconButtonColors.containerColor(enabled: Boolean): Color =
    if(enabled) containerColor else disabledContainerColor