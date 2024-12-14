package com.mrboomdev.awery.ui.mobile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.i18n
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.platform.PlatformSetting
import com.mrboomdev.awery.platform.PlatformSettingHandler

@Composable
fun MobileSetting(
	setting: Setting,
	onOpenScreen: (Setting) -> Unit,
	isSelected: Boolean = false
) {
	var triState by remember { mutableStateOf(setting.value as? Setting.TriState ?: Setting.TriState.EMPTY) }
	var isChecked by remember { mutableStateOf(setting.value == true) }
	val context = LocalContext.current

	Surface(
		enabled = setting.type != null && setting.type != Setting.Type.CATEGORY,
		shape = RoundedCornerShape(16.dp),

		color = if(isSelected) {
			MaterialTheme.colorScheme.contentColorFor(MaterialTheme.colorScheme.background)
		} else Color.Transparent,

		contentColor = if(isSelected) {
			MaterialTheme.colorScheme.background
		} else contentColorFor(MaterialTheme.colorScheme.surface),

		onClick = {
			when(setting.type) {
				Setting.Type.SCREEN, Setting.Type.SCREEN_BOOLEAN -> {
					onOpenScreen(setting)
				}

				Setting.Type.ACTION -> {
					if(setting is PlatformSetting) {
						PlatformSettingHandler.handlePlatformClick(context, setting)
					} else {
						setting.onClick()
					}
				}

				Setting.Type.BOOLEAN -> isChecked = !isChecked
				Setting.Type.TRI_STATE -> triState = triState.next()

				Setting.Type.FLOAT -> toast("This action isn't done yet!")
				Setting.Type.STRING -> toast("This action isn't done yet!")
				Setting.Type.SELECT -> toast("This action isn't done yet!")
				Setting.Type.INTEGER -> toast("This action isn't done yet!")
				Setting.Type.MULTISELECT -> toast("This action isn't done yet!")

				Setting.Type.CATEGORY, null -> {}
			}
		}
	) {
		Row(
			modifier = Modifier.padding(end = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Column(
				modifier = Modifier
					.weight(1f)
					.padding(16.dp)
			) {
				(setting.title ?: (if(setting.description == null) setting.key else null))?.let { title ->
					Text(
						style = MaterialTheme.typography.bodyLarge,
						text = setting.takeIf { it is PlatformSetting }?.let {
							i18n<R.string>(title)
						} ?: title
					)
				}

				setting.description?.let { description ->
					if(setting.title != null) {
						Spacer(Modifier.padding(3.dp))
					}

					Text(
						style = if(setting.title == null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
						color = if(setting.type == Setting.Type.CATEGORY) MaterialTheme.colorScheme.primary else Color.Unspecified,

						text = setting.takeIf { it is PlatformSetting }?.let {
							i18n<R.string>(description)
						} ?: description
					)
				}
			}

			if(setting.type == Setting.Type.TRI_STATE) {
				TriStateCheckbox(
					state = triState.asToggleableState(),
					onClick = {
						triState = triState.next()
						setting.value = triState
					}
				)
			}

			if(setting.type == Setting.Type.BOOLEAN || setting.type == Setting.Type.SCREEN_BOOLEAN) {
				Switch(
					checked = isChecked,
					onCheckedChange = {
						isChecked = it
						setting.value = it
					}
				)
			}
		}
	}
}

private fun Setting.TriState.asToggleableState() = when(this) {
	Setting.TriState.EMPTY -> ToggleableState.Indeterminate
	Setting.TriState.CHECKED -> ToggleableState.On
	Setting.TriState.UNCHECKED -> ToggleableState.Off
}

private fun Setting.TriState.next() = when(this) {
	Setting.TriState.EMPTY -> Setting.TriState.CHECKED
	Setting.TriState.CHECKED -> Setting.TriState.UNCHECKED
	Setting.TriState.UNCHECKED -> Setting.TriState.EMPTY
}