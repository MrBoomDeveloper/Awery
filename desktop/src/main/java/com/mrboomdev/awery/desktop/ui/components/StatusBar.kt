package com.mrboomdev.awery.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import kotlin.system.exitProcess

@Composable
fun StatusBar(
	title: String,
	windowScope: FrameWindowScope
) {
	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				fontSize = 15.sp,
				text = title,
				modifier = Modifier
					.padding(24.dp, 0.dp)
			)

			Spacer(modifier = Modifier.weight(1f))

			Button(
				onClick = { windowScope.window.isMinimized = true },
				text = "Minimize",
				icon = painterResource("ic_minimize.xml"),
				hoverColor = Color(0x11AAAAAA),
				pressColor = Color(0x22AAAAAA)
			)

			Button(
				onClick = {
					windowScope.window.placement = if(windowScope.window.placement == WindowPlacement.Maximized)
					WindowPlacement.Floating else WindowPlacement.Maximized
				},

				text = "Fullscreen",
				icon = painterResource("ic_select_window_2.xml"),
				hoverColor = Color(0x11AAAAAA),
				pressColor = Color(0x22AAAAAA)
			)

			Button(
				onClick = { exitProcess(0) },
				text = "Close",
				icon = painterResource("ic_close.xml"),
				hoverColor = Color(0xffff0000)
			)
		}

		Divider()
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Button(
	modifier: Modifier = Modifier,
	text: String,
	icon: Painter? = null,
	onClick: () -> Unit,
	hoverColor: Color,
	pressColor: Color = hoverColor
) {
	var isHovered by remember { mutableStateOf(false) }
	var isPressed by remember { mutableStateOf(false) }

	Box(
		modifier = modifier
			.background(if(isHovered) {
				if(isPressed) pressColor else hoverColor
			} else Color.Transparent)
			.pointerHoverIcon(PointerIcon.Hand)
			.onPointerEvent(PointerEventType.Enter) { isHovered = true }
			.onPointerEvent(PointerEventType.Exit) { isHovered = false }
			.onPointerEvent(PointerEventType.Press) { isPressed = true }
			.onPointerEvent(PointerEventType.Release) { isPressed = false }
			.clickable { onClick() }
	) {
		if(icon != null) {
			Image(
				painter = icon,
				contentDescription = text,
				modifier = Modifier.height(35.dp)
					.width(50.dp)
					.padding(10.dp)
			)
		} else {
			Text(
				modifier = Modifier.padding(24.dp, 8.dp),
				text = text
			)
		}
	}
}