package com.mrboomdev.awery.ui.routes

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable

@Serializable
class NotificationsRoute: BaseRoute() {
	@Composable
	override fun Content() {
		Text("Notifications")
	}
}