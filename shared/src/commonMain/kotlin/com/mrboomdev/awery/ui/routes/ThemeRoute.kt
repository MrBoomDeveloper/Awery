package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class ThemeRoute: BaseRoute() {
	@Composable
	override fun Content() {
		Column {
			Text("Theming...")
		}
	}
}