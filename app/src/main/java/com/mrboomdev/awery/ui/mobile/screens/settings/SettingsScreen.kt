package com.mrboomdev.awery.ui.mobile.screens.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ui.mobile.AweryTheme

class SettingsActivity2: AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			AweryTheme {
				SettingsScreen()
			}
		}
	}
}

@Composable
fun SettingsScreen() {
	SettingsPanel(Setting())
}

@Composable
fun SettingsPanel(setting: Setting) {
	Text("Hello, World!")
}