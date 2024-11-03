package com.mrboomdev.awery.desktop

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mrboomdev.awery.desktop.ui.components.StatusBar
import java.awt.Dimension
import kotlin.system.exitProcess

fun main() = application {
	Window(
		title = "Awery",
		undecorated = true,
		transparent = true,
		onCloseRequest = { exitApplication() }
	) {
		remember {
			window.minimumSize = Dimension(500, 350)
			window.requestFocus()
		}

		WindowDraggableArea(
			modifier = Modifier.clip(AbsoluteRoundedCornerShape(4.dp))
		) {
			MaterialTheme(colors = darkColors()) {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background
				) {
					Column {
						StatusBar(
							title = "Awery",
							windowScope = this@Window
						)

						Button(
							modifier = Modifier.pointerHoverIcon(PointerIcon.Hand),
							onClick = { println("Hello, World!") }
						) {
							Text("Hello, Everynyan UwU")
						}
					}
				}
			}
		}
	}
}