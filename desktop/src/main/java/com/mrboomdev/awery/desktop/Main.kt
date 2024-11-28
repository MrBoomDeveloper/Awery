package com.mrboomdev.awery.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mrboomdev.awery.desktop.ui.components.StatusBar
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JWindow
import javax.swing.SwingUtilities
import javax.swing.WindowConstants
import kotlin.system.exitProcess

/*fun main() = SwingUtilities.invokeLater {
	val window = JFrame()
	window.type = java.awt.Window.Type.UTILITY

	val panel = ComposePanel()
	panel.setContent {
		val state = rememberWindowState(placement = WindowPlacement.Floating)

		MaterialTheme(colors = darkColors()) {
			Surface(
				modifier = Modifier.fillMaxSize(),
				color = MaterialTheme.colors.background
			) {
				Column {
					StatusBar(
						title = "Awery",
						windowState = state,
						onCloseRequest = { exitProcess(0) }
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

	window.contentPane.add(panel, BorderLayout.CENTER)
	window.size = Dimension(500, 350)
	window.isVisible = true
}*/

fun main() = application {
	val state = rememberWindowState(placement = WindowPlacement.Floating)

	Window(
		title = "Awery",
		undecorated = true,
		state = state,
		onCloseRequest = ::exitApplication
	) {
		remember {
			window.minimumSize = Dimension(500, 350)
		}

		WindowDraggableArea {
			MaterialTheme(colors = darkColors()) {
				Surface(
					modifier = Modifier.fillMaxSize(),
					color = MaterialTheme.colors.background
				) {
					Column {
						StatusBar(
							title = "Awery",
							windowState = state,
							onCloseRequest = ::exitApplication
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