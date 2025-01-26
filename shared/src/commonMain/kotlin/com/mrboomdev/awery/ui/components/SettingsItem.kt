package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dokar.sonner.TextToastAction
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.LocalSettingHandler
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.platform.drawableResource
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.utils.LocalToaster
import com.mrboomdev.awery.utils.compareTo
import com.mrboomdev.awery.utils.toStrippedString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun SettingsItem(
	setting: Setting,
	isSelected: Boolean = false
) {
	val settingHandler = LocalSettingHandler.current
	val toaster = LocalToaster.current
	var isChecked by remember(setting) { mutableStateOf(setting.value == true) }
	var isDialogShown by remember(setting) { mutableStateOf(false) }
	
	var triState by remember(setting) { mutableStateOf(
		setting.value as? Setting.TriState ?: Setting.TriState.EMPTY) }
	
	fun suggestToRestart() {
		toaster.show(
			message = i18n(Res.string.restart_to_apply_settings),
			duration = 10.toDuration(DurationUnit.SECONDS),
			action = TextToastAction(
				text = i18n(Res.string.restart),
				onClick = { Platform.restartApp() }
			)
		)
	}
	
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
					settingHandler.openScreen(setting)
				}
				
				Setting.Type.ACTION -> {
					if(setting is PlatformSetting) {
						settingHandler.handleClick(setting)
						
						if(setting.restart) {
							suggestToRestart()
						}
					} else {
						setting.onClick()
					}
				}
				
				Setting.Type.BOOLEAN -> {
					isChecked = !isChecked
					setting.value = isChecked
					
					if(setting is PlatformSetting && setting.restart) {
						suggestToRestart()
					}
				}
				
				Setting.Type.TRI_STATE -> {
					triState = triState.next()
					setting.value = triState
					
					if(setting is PlatformSetting && setting.restart) {
						suggestToRestart()
					}
				}
				
				Setting.Type.FLOAT,
				Setting.Type.STRING,
				Setting.Type.SELECT,
				Setting.Type.INTEGER,
				Setting.Type.MULTISELECT -> isDialogShown = true
				
				Setting.Type.CATEGORY, null -> {}
			}
		}
	) {
		Row(
			modifier = Modifier.padding(horizontal = 16.dp).heightIn(min = 64.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			val painter = if(setting is PlatformSetting) {
				setting.iconRes?.let { drawableResource(it) }?.let { icon ->
					painterResource(icon)
				}
			} else null
			
			when {
				// Only VectorPainter is looking with with applied tinting
				painter is VectorPainter -> Icon(
					modifier = Modifier.padding(end = 16.dp).size(32.dp),
					painter = painter,
					contentDescription = null
				)
				
				painter != null -> Image(
					modifier = Modifier.padding(end = 16.dp).size(36.dp),
					painter = painter,
					contentDescription = null
				)
			}
			
			Column(
				modifier = Modifier.weight(1f).padding(vertical = 16.dp)
			) {
				(setting.title ?: (if(setting.description == null) setting.key else null))?.let { title ->
					Text(
						style = MaterialTheme.typography.bodyLarge,
						text = setting.takeIf { it is PlatformSetting }?.let { i18n(title) } ?: title
					)
				}
				
				setting.description?.let { description ->
					if(setting.title != null) {
						Spacer(Modifier.padding(3.dp))
					}
					
					Text(
						style = if(setting.title == null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
						color = if(setting.type == Setting.Type.CATEGORY) MaterialTheme.colorScheme.primary else Color.Unspecified,
						text = setting.takeIf { it is PlatformSetting }?.let { i18n(description) } ?: description
					)
				}
			}
			
			if(setting.type == Setting.Type.TRI_STATE) {
				TriStateCheckbox(
					state = triState.asToggleableState(),
					onClick = {
						triState = triState.next()
						setting.value = triState
						
						if(setting is PlatformSetting && setting.restart) {
							suggestToRestart()
						}
					}
				)
			}
			
			if(setting.type == Setting.Type.BOOLEAN || setting.type == Setting.Type.SCREEN_BOOLEAN) {
				Switch(
					checked = isChecked,
					onCheckedChange = {
						isChecked = it
						setting.value = it
						
						if(setting is PlatformSetting && setting.restart) {
							suggestToRestart()
						}
					}
				)
			}
		}
	}
	
	if(isDialogShown) {
		var newValue by remember { mutableStateOf(setting.value) }
		
		val isValidValue by remember { derivedStateOf {
			when(setting.type) {
				Setting.Type.STRING -> {
					setting.from?.also { from ->
						if(((newValue as? String?)?.length ?: 0) < from) {
							return@derivedStateOf false to "This text is too short! Minimum length is ${from.toStrippedString()}."
						}
					}
					
					setting.to?.also { to ->
						if(((newValue as? String?)?.length ?: 0) > to) {
							return@derivedStateOf false to "This text is too long! Maximum length is ${to.toStrippedString()}."
						}
					}
					
					true to ""
				}
				
				Setting.Type.INTEGER, Setting.Type.FLOAT -> {
					setting.from?.also { from ->
						if((newValue as? Number? ?: 0) < from) {
							return@derivedStateOf false to "This number is too short! Minimum length is ${from.toStrippedString()}."
						}
					}
					
					setting.to?.also { to ->
						if((newValue as? Number? ?: 0) > to) {
							return@derivedStateOf false to "This number is too long! Maximum length is ${to.toStrippedString()}."
						}
					}
					
					if(newValue != null && newValue !is Number) {
						return@derivedStateOf false to "This is not a number!"
					}
					
					true to ""
				}
				
				else -> true to "No checks can be made on this type."
			}
		}}
		
		MaterialDialog(
			modifier = Modifier.padding(horizontal = 8.dp),
			onDismissRequest = { isDialogShown = false },
			
			title = setting.title?.let { title -> {
				Text(
					style = MaterialTheme.typography.headlineMedium,
					text = setting.takeIf { it is PlatformSetting }?.let { i18n(title) } ?: title
				)
			}},
			
			dismissButton = {
				TextButton(onClick = this@MaterialDialog::requestDismiss) {
					Text(text = stringResource(Res.string.cancel))
				}
			},
			
			confirmButton = {
				TextButton(onClick = {
					if(!isValidValue.first) return@TextButton
					setting.value = newValue
					requestDismiss()
					
					if(setting is PlatformSetting && setting.restart) {
						suggestToRestart()
					}
				}) {
					Text(text = stringResource(Res.string.confirm))
				}
			}
		) {
			Column {
				if(setting.description != null) {
					val description = setting.description!!
					
					Text(
						text = setting.takeIf { it is PlatformSetting }
							?.let { i18n(description) } ?: description
					)
				}
				
				when(setting.type) {
					Setting.Type.STRING -> {
						OutlinedTextField(
							isError = isValidValue.first,
							label = if(isValidValue.first) null else {{
								Text(isValidValue.second)
							}},
							
							placeholder = if(setting is PlatformSetting && setting.placeholder != null) {{
								Text(setting.placeholder)
							}} else null,
							
							singleLine = true,
							value = newValue?.toString() ?: "",
							onValueChange = { newValue = it },
							keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
							keyboardActions = KeyboardActions(onDone = {
								if(!isValidValue.first) return@KeyboardActions
								setting.value = newValue
								requestDismiss()
							})
						)
					}
					
					Setting.Type.INTEGER -> {
						OutlinedTextField(
							isError = !isValidValue.first,
							label = if(isValidValue.first) null else {{
								Text(isValidValue.second)
							}},
							
							placeholder = if(setting is PlatformSetting && setting.placeholder != null) {{
								Text(setting.placeholder)
							}} else null,
							
							singleLine = true,
							value = newValue?.toString() ?: "",
							
							onValueChange = {
								newValue = if(it.isBlank()) null else try {
									it.toInt()
								} catch(e: NumberFormatException) { it }
							},
							
							keyboardOptions = KeyboardOptions(
								keyboardType = KeyboardType.Phone,
								imeAction = ImeAction.Done
							),
							
							keyboardActions = KeyboardActions(onDone = {
								if(!isValidValue.first) return@KeyboardActions
								setting.value = newValue
								requestDismiss()
							})
						)
					}
					
					Setting.Type.FLOAT -> {
						OutlinedTextField(
							isError = !isValidValue.first,
							label = if(isValidValue.first) null else {{
								Text(isValidValue.second)
							}},
							
							placeholder = if(setting is PlatformSetting && setting.placeholder != null) {{
								Text(setting.placeholder)
							}} else null,
							
							singleLine = true,
							value = newValue?.toString() ?: "",
							
							onValueChange = {
								newValue = if(it.isBlank()) null else try {
									it.toFloat()
								} catch(e: NumberFormatException) { it }
							},
							
							keyboardOptions = KeyboardOptions(
								keyboardType = KeyboardType.Decimal,
								imeAction = ImeAction.Done
							),
							
							keyboardActions = KeyboardActions(onDone = {
								setting.value = newValue
								requestDismiss()
							})
						)
					}
					
					Setting.Type.SELECT -> {
						Column(modifier = Modifier.selectableGroup()) {
							for(item in setting.items!!) {
								Row(modifier = Modifier
									.clip(RoundedCornerShape(8.dp))
									.fillMaxWidth()
									.height(56.dp)
									.selectable(
										selected = newValue == item.key,
										onClick = { newValue = item.key },
										role = Role.RadioButton
									),
									verticalAlignment = Alignment.CenterVertically
								) {
									RadioButton(
										selected = newValue == item.key,
										onClick = null
									)
									
									Text(
										text = item.title?.let { title ->
											setting.takeIf { it is PlatformSetting }?.let { i18n(title) } ?: title
										} ?: item.key ?: "No title",
										
										style = MaterialTheme.typography.bodyLarge,
										modifier = Modifier.padding(start = 16.dp)
									)
								}
							}
						}
					}
					
					else -> {
						Text(
							style = MaterialTheme.typography.bodyLarge,
							color = Color.Red,
							text = "Unsupported setting type!"
						)
					}
				}
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