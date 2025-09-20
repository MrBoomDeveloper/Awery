package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.BooleanPreference
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.LabelPreference
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.PreferenceGroup
import com.mrboomdev.awery.extension.sdk.SelectPreference
import com.mrboomdev.awery.extension.sdk.StringPreference
import com.mrboomdev.awery.extension.sdk.modules.ManageableModule
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.adult_content
import com.mrboomdev.awery.resources.cancel
import com.mrboomdev.awery.resources.ic_explict_outlined
import com.mrboomdev.awery.resources.ok
import com.mrboomdev.awery.ui.components.AlertDialog
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import com.mrboomdev.awery.ui.components.DefaultExtImage
import com.mrboomdev.awery.ui.components.ExtImage
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemDialog
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.bottom
import com.mrboomdev.awery.ui.utils.classify
import com.mrboomdev.awery.ui.utils.end
import com.mrboomdev.awery.ui.utils.singleItem
import com.mrboomdev.awery.ui.utils.start
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private data class State(
	val extension: Extension,
	private val module: ManageableModule
) {
	private val _prefs = mutableStateOf(module.getPreferences())
	val preferences by _prefs
	
	suspend fun uninstall() = module.uninstall()
	fun savePrefs() {
		module.onSavePreferences(preferences)
		_prefs.value = module.getPreferences()
	}
	
	companion object {
		suspend fun of(id: String): State? {
			val extension = Extensions[id]
			val module = extension?.get<ManageableModule>()
			
			return if(extension != null && module != null) {
				State(extension, module)
			} else null
		}
	}
}

@Composable
fun SettingsExtensionPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onBack: (() -> Unit)?,
	id: String
) {
	val coroutineScope = rememberCoroutineScope()
	val toaster = LocalToaster.current
	var isLoading by remember(coroutineScope) { mutableStateOf(false) }
	
	val state by produceState<State?>(null, id) {
		value = State.of(id) ?: run { 
			onBack?.invoke()
			return@produceState
		}
	}
	
	if(isLoading) {
		Dialog(onDismissRequest = {}) { CircularProgressIndicator() }
	}
	
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text("Extension settings") }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding.add(bottom = 16.dp)
		) { 
			singleItem("scrollFixer")
			
			state?.also { state ->
				singleItem("header") {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 18.dp, vertical = 4.dp)
							.animateItem(),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						state.extension.icon?.also { icon ->
							ExtImage(Modifier.size(48.dp), icon)
						} ?: run {
							DefaultExtImage(Modifier.size(48.dp))
						}

						Column(
							modifier = Modifier.weight(1f),
							verticalArrangement = Arrangement.spacedBy(2.dp)
						) {
							Text(
								color = MaterialTheme.colorScheme.onBackground,
								text = state.extension.name
							)

							Text(
								style = MaterialTheme.typography.bodySmall,
								color = MaterialTheme.colorScheme.onSurfaceVariant,
								
								inlineContent = remember { 
									mapOf("nsfw" to InlineTextContent(
										Placeholder(
											width = 14.sp, 
											height = 14.sp, 
											placeholderVerticalAlign = PlaceholderVerticalAlign.Center
										)
									) { 
										Icon(
											painter = painterResource(Res.drawable.ic_explict_outlined), 
											contentDescription = stringResource(Res.string.adult_content)
										)
									})
								},
								
								text = buildAnnotatedString {
									if(state.extension.isNsfw) {
										appendInlineContent("nsfw", "NSFW")
										append(" ")
									}
									
									append(state.extension.version)
									
									if(AwerySettings.showIds.state.value) {
										append(" ")
										append(state.extension.id)
									}
								}
							)
						}

						OutlinedButton(
							onClick = {
								coroutineScope.launch {
									isLoading = true

									try {
										state.uninstall()
										onBack?.invoke()
									} catch(_: CancellationException) {
										Log.e("SettingsExtensionPage", "Failed to uninstall an extension! Operation cancelled.")
										isLoading = false
									} catch(t: Throwable) {
										Log.e("SettingsExtensionPage", "Failed to uninstall an extension!", t)

										toaster.toast(
											title = t.classify().title,
											message = t.classify().message,
											duration = 7_500
										)

										isLoading = false
									}
								}
							}
						) { Text("Uninstall") }
					}
				}
				
				items(
					items = state.preferences,
					key = { it.key }
				) { item ->
					@Composable
					fun Item(item: Preference<*>) {
						var value by remember(item) { mutableStateOf(item.value) }
						
						val padding = remember {
							PaddingValues(horizontal = 18.dp, vertical = 10.dp)
						}

						fun updateValue(newValue: Any?) {
							value = newValue

							@Suppress("UNCHECKED_CAST")
							(item as Preference<Any?>).value = newValue

							state.savePrefs()
						}
						
						when(val item = item) {
							is StringPreference -> {
								SettingsDefaults.itemDialog(
									title = item.name,
									description = item.description,
									contentPadding = padding
								) {
									var newValue by remember { mutableStateOf(item.value) }

									AlertDialog(
										onDismissRequest = ::dismiss,
										contentPadding = PaddingValues(vertical = 6.dp),
										title = { Text(item.name) },

										confirmButton = {
											TextButton({
												updateValue(newValue)
												dismiss()
											}) {
												Text(stringResource(Res.string.ok))
											}
										},

										cancelButton = {
											TextButton(::dismiss) {
												Text(stringResource(Res.string.cancel))
											}
										}
									) {
										val focusRequester = remember { FocusRequester() }

										LaunchedEffect(Unit) {
											focusRequester.requestFocus()
										}

										OutlinedTextField(
											modifier = Modifier
												.fillMaxWidth()
												.focusRequester(focusRequester)
												.padding(horizontal = 24.dp, vertical = 16.dp),

											value = newValue,
											onValueChange = { newValue = it },
											singleLine = true,

											keyboardOptions = KeyboardOptions(
												imeAction = ImeAction.Done
											),

											keyboardActions = KeyboardActions(
												onDone = {
													updateValue(newValue)
													dismiss()
												}
											)
										)
									}
								}
							}

							is BooleanPreference -> {
								val interactionSource = remember { MutableInteractionSource() }

								SettingsDefaults.itemCustom(
									modifier = Modifier.toggleable(
										value = value as Boolean,
										onValueChange = { updateValue(it) },
										interactionSource = interactionSource,
										indication = LocalIndication.current
									).animateItem(),

									title = {
										Text(
											color = MaterialTheme.colorScheme.onBackground,
											text = item.name
										)
									},

									description = item.description?.let {{
										Text(it)
									}},
									
									contentPadding = padding
								) {
									Switch(
										checked = value as Boolean,
										onCheckedChange = { updateValue(it) },
										interactionSource = interactionSource
									)
								}
							}

							is LabelPreference -> SettingsDefaults.itemCustom(
								modifier = Modifier.animateItem(),
								contentPadding = padding,
								
								title = {
									Text(
										color = MaterialTheme.colorScheme.onBackground,
										text = item.name
									)
								},

								description = item.description?.let {{
									Text(it)
								}}
							)

							is SelectPreference -> {
								SettingsDefaults.itemDialog(
									title = item.name,
									description = item.description,
									contentPadding = padding
								) {
									var newValue by remember { mutableStateOf(item.value) }

									AlertDialog(
										onDismissRequest = ::dismiss,
										contentPadding = PaddingValues(vertical = 6.dp),
										title = { Text(item.name) },

										confirmButton = {
											TextButton({
												updateValue(newValue)
												dismiss()
											}) {
												Text(stringResource(Res.string.ok))
											}
										},

										cancelButton = {
											TextButton(::dismiss) {
												Text(stringResource(Res.string.cancel))
											}
										}
									) {
										LazyColumn(Modifier.fillMaxWidth()) {
											items(
												items = item.values,
												key = { it.key }
											) { (value, name) ->
												val interactionSource = remember { MutableInteractionSource() }

												Row(
													modifier = Modifier
														.clickable(
															interactionSource = interactionSource,
															indication = LocalIndication.current,
															role = Role.RadioButton,
															onClick = { newValue = value }
														).fillMaxWidth()
														.padding(horizontal = 24.dp, vertical = 1.dp),
													horizontalArrangement = Arrangement.spacedBy(8.dp),
													verticalAlignment = Alignment.CenterVertically
												) {
													Text(name)
													Spacer(Modifier.weight(1f))
													RadioButton(
														interactionSource = interactionSource,
														selected = newValue == value,
														onClick = { newValue = value }
													)
												}
											}
										}
									}
								}
							}

							is PreferenceGroup -> SettingsDefaults.itemDialog(
								title = item.name,
								description = item.description,
								contentPadding = padding
							) {
								BottomSheetDialog(::dismiss) {
									LazyColumn(
										modifier = Modifier.fillMaxWidth(),
										contentPadding = PaddingValues(vertical = 16.dp)
									) {
										singleItem("header") {
											Text(
												modifier = Modifier.padding(
													start = padding.start, 
													end = padding.end, 
													bottom = padding.bottom
												),
												
												style = MaterialTheme.typography.titleLarge,
												text = item.name
											)
										}

										items(
											items = item.items,
											key = { it.key }
										) { Item(it) }
									}
								}
							}

							else -> {
								Text(
									modifier = Modifier
										.padding(16.dp)
										.animateItem(),
									color = MaterialTheme.colorScheme.error,
									text = "${item::class.qualifiedName} isn't supported here yet."
								)
							}
						}
					}
					
					Item(item)
				}
			}
		}
	}
}