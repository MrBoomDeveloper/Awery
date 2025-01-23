package com.mrboomdev.awery.desktop

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.FadeTransition
import com.mrboomdev.awery.desktop.ui.components.StatusBar
import com.mrboomdev.awery.ui.routes.SplashRoute
import java.awt.Dimension

/*fun jvmMain() = SwingUtilities.invokeLater {
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
						
						Navigator(
							screen = SplashRoute(),
							disposeBehavior = NavigatorDisposeBehavior(disposeSteps = false)
						) { navigator ->
							@OptIn(ExperimentalVoyagerApi::class)
							FadeTransition(
								navigator = navigator, 
								disposeScreenAfterTransitionEnd = true
							)
						}
					}
				}
			}
		}
	}
}