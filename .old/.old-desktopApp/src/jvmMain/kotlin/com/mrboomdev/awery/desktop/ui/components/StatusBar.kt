package com.mrboomdev.awery.desktop.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlin.system.exitProcess

@Composable
fun StatusBar(
	title: String,
	windowState: WindowState,
	onCloseRequest: () -> Unit = { exitProcess(0) }
) {
	Column {
		Row(
			modifier = Modifier.height(40.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				fontSize = 15.sp,
				text = title,
				modifier = Modifier.padding(24.dp, 0.dp)
			)

			Spacer(modifier = Modifier.weight(1f))

			Button(
				hoverColor = Color(0x11AAAAAA),
				pressColor = Color(0x22AAAAAA),
				onClick = { windowState.isMinimized = true }
			) {
				Image(
					modifier = Modifier.fillMaxHeight()
						.aspectRatio(1.4f)
						.padding(12.dp),

					painter = painterResource("icon/minimize.xml"),
					contentDescription = "Minimize")
			}

			Button(
				hoverColor = Color(0x11AAAAAA),
				pressColor = Color(0x22AAAAAA),
				onClick = {
					windowState.placement = if(windowState.placement == WindowPlacement.Maximized) {
						WindowPlacement.Floating
					} else WindowPlacement.Maximized
				}
			) {
				Image(
					modifier = Modifier.fillMaxHeight()
						.aspectRatio(1.4f)
						.padding(12.dp),

					painter = painterResource("icon/select_window_2.xml"),
					contentDescription = "Fullscreen")
			}

			Button(
				hoverColor = Color(0xffff0000),
				onClick = onCloseRequest
			) {
				Image(
					modifier = Modifier.fillMaxHeight()
						.aspectRatio(1.4f)
						.padding(11.dp),

					painter = painterResource("icon/close.xml"),
					contentDescription = "Close")
			}
		}

		Divider()
	}
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Button(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	hoverColor: Color,
	pressColor: Color = hoverColor,
	content: @Composable () -> Unit
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
		content()
	}
}