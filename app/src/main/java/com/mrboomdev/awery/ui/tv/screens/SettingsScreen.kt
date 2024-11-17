package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.Text
import androidx.tv.material3.WideButton
import com.mrboomdev.awery.app.data.settings.SettingsItem
import kotlinx.serialization.Serializable

@Composable
fun SettingsScreen(
	screen: SettingsItem
) {
	Box(modifier = Modifier.fillMaxSize()) {
		WideButton(onClick = {}) {
			Text(text = "Test setting")
		}
	}
}