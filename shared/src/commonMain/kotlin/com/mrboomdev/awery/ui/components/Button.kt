package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

@Composable
fun ButtonWithIcon(
	modifier: Modifier = Modifier,
	icon: Painter,
	iconSize: Dp = ButtonDefaults.IconSize,
	text: String,
	onClick: () -> Unit
) {
	Button(
		modifier = modifier,
		contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
		onClick = onClick
	) {
		Icon(
			modifier = Modifier.size(iconSize),
			painter = icon,
			contentDescription = null
		)
		
		Spacer(Modifier.width(ButtonDefaults.IconSpacing))
		
		Text(text)
	}
}