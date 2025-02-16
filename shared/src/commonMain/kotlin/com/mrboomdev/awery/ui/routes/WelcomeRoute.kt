package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class WelcomeRoute: BaseRoute() {
	@Composable
	override fun Content() {
		Column { 
			Text("Welcome to Awery")
		}
	}
}