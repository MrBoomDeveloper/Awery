package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ButtonBorder
import androidx.tv.material3.ButtonColors
import androidx.tv.material3.ButtonGlow
import androidx.tv.material3.ButtonScale
import androidx.tv.material3.ButtonShape
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults

@NonRestartableComposable
@Composable
fun TvIconButton(
	onClick: () -> Unit,
	painter: Painter,
	contentDescription: String?,
	padding: Dp = 8.dp,
	modifier: Modifier = Modifier,
	onLongClick: (() -> Unit)? = null,
	enabled: Boolean = true,
	scale: ButtonScale = IconButtonDefaults.scale(),
	glow: ButtonGlow = IconButtonDefaults.glow(),
	shape: ButtonShape = IconButtonDefaults.shape(),
	colors: ButtonColors = IconButtonDefaults.colors(),
	border: ButtonBorder = IconButtonDefaults.border(),
	interactionSource: MutableInteractionSource? = null
) {
	IconButton(
		onClick = onClick,
		modifier = modifier,
		onLongClick = onLongClick,
		enabled = enabled,
		scale = scale,
		glow = glow,
		shape = shape,
		colors = colors,
		border = border,
		interactionSource = interactionSource
	) {
		Icon(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			painter = painter,
			contentDescription = contentDescription
		)
	}
}