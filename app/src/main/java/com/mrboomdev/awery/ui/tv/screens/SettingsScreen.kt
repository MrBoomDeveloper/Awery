package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.tv.material3.Text
import androidx.tv.material3.WideButton
import com.mrboomdev.awery.data.settings.SettingsItem

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