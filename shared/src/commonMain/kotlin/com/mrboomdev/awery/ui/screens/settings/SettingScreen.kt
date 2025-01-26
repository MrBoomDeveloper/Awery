package com.mrboomdev.awery.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.ui.components.SettingsItem

@Composable
fun SettingScreen(
	modifier: Modifier = Modifier,
	screen: Setting,
	selected: Collection<Setting>? = null,
	header: (@Composable () -> Unit)? = null
) {
	LazyColumn(modifier = modifier) {
		if(header != null) {
			item("header") {
				header()
			}
		}
		
		if(screen.items.isNullOrEmpty()) {
			item("empty") {
				Column(
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Text(
						text = "There is nothing",
						color = Color.White
					)
				}
			}
		} else {
			items(
				items = screen.items!!,
				key = { it.key ?: it },
				contentType = { it.type }
			) {
				if(!it.isVisible) return@items
				
				SettingsItem(
					setting = it, 
					isSelected = selected != null && it in selected
				)
			}
		}

		item("bottomPadding") {
			Spacer(Modifier.height(50.dp))
		}
	}
}