package com.mrboomdev.awery.ui.screens.extension

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.utils.next
import com.mrboomdev.awery.extension.sdk.*
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.components.AlertDialog
import com.mrboomdev.awery.ui.components.BottomSheetDialog
import com.mrboomdev.awery.ui.utils.scaleX
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
private fun FilterGroup(
	modifier: Modifier,
	filters: List<Pair<Preference<*>, MutableState<*>>>
) {
	LazyColumn(
		modifier = modifier,
		contentPadding = PaddingValues(vertical = 4.dp)
	) {
		items(
			items = filters.filter { it.first.role != Preference.Role.QUERY },
			key = { it.first.key },
			contentType = { it.first.role?.name ?: it::class.simpleName }
		) { (filter, value) ->
			val interactionSource = remember { MutableInteractionSource() }

			@Suppress("UNCHECKED_CAST")
			fun updateValue(newValue: Any) {
				(value as MutableState<Any>).value = newValue
				(filter as Preference<Any>).value = value.value
			}

			Box {
				var showDialog by remember { mutableStateOf(false) }

				Row(
					modifier = Modifier.clickable(
						interactionSource = interactionSource,
						indication = LocalIndication.current,
						enabled = filter !is LabelPreference,
						onClick = {
							when(filter) {
								is SelectPreference,
								is StringPreference, 
								is PreferenceGroup -> 
									showDialog = true

								is BooleanPreference ->
									updateValue(!(value.value as Boolean))

								is TriStatePreference ->
									updateValue((value.value as TriStatePreference.State).next())

								else -> {}
							}
						}).padding(horizontal = 32.dp, 2.dp)
						.heightIn(min = 48.dp),
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						modifier = Modifier.weight(1f),
						text = filter.name,

						style = when(filter) {
							is LabelPreference -> MaterialTheme.typography.titleMedium
							else -> MaterialTheme.typography.bodyLarge
						}
					)

					when(filter) {
						is StringPreference -> {
							Text(
								color = MaterialTheme.colorScheme.primary,
								text = filter.value
							)
						}

						is SelectPreference -> {
							Text(
								color = MaterialTheme.colorScheme.primary,
								text = filter.values.first { it.key == value.value.toString() }.name
							)
						}

						is BooleanPreference -> {
							Switch(
								checked = value.value as Boolean,
								onCheckedChange = { updateValue(it) },
								interactionSource = interactionSource
							)
						}

						is TriStatePreference -> {
							TriStateCheckbox(
								modifier = Modifier.offset(x = 12.dp),

								state = when(value.value as TriStatePreference.State) {
									TriStatePreference.State.INCLUDED -> ToggleableState.On
									TriStatePreference.State.EXCLUDED -> ToggleableState.Indeterminate
									TriStatePreference.State.NONE -> ToggleableState.Off
								},

								onClick = { updateValue((value.value as TriStatePreference.State).next()) },
								interactionSource = interactionSource
							)
						}

						is PreferenceGroup -> {
							Icon(
								modifier = Modifier
									.size(24.dp)
									.scaleX(-1f),
								painter = painterResource(Res.drawable.ic_back),
								contentDescription = null
							)
						}

						is LabelPreference -> {}

						else -> {
							Text(
								modifier = Modifier.weight(1f),
								color = MaterialTheme.colorScheme.error,
								text = "${filter::class.qualifiedName} isn't supported here yet."
							)
						}
					}
				}

				DropdownMenu(
					expanded = showDialog && filter is SelectPreference,
					onDismissRequest = { showDialog = false }
				) {
					if(filter !is SelectPreference) return@DropdownMenu

					filter.values.forEach {
						DropdownMenuItem(
							text = {
								Text(
									modifier = Modifier.padding(horizontal = 8.dp),
									text = it.name
								)
							},

							onClick = {
								updateValue(it.key)
								showDialog = false
							}
						)
					}
				}

				if(showDialog && filter is StringPreference) {
					var editTextState by rememberSaveable { mutableStateOf(value.value.toString()) }

					AlertDialog(
						onDismissRequest = { showDialog = false },
						contentPadding = PaddingValues(vertical = 6.dp),
						title = { Text(filter.name) },

						confirmButton = {
							TextButton(onClick = {
								updateValue(editTextState)
								showDialog = false
							}) {
								Text(stringResource(Res.string.ok))
							}
						},

						cancelButton = {
							TextButton({ showDialog = false }) {
								Text(stringResource(Res.string.cancel))
							}
						}
					) {
						val focusRequester = remember { FocusRequester() }

						LaunchedEffect(Unit) {
							focusRequester.requestFocus()
						}

						Spacer(Modifier.height(6.dp))

						OutlinedTextField(
							modifier = Modifier
								.fillMaxWidth()
								.focusRequester(focusRequester)
								.padding(horizontal = 24.dp),

							value = editTextState,
							onValueChange = { editTextState = it },
							singleLine = true,
							keyboardOptions = KeyboardOptions(
								imeAction = ImeAction.Done
							),
							keyboardActions = KeyboardActions(
								onDone = { updateValue(editTextState) }
							)
						)
					}
				}
				
				if(showDialog && filter is PreferenceGroup) {
					val items = remember(filter) {
						filter.items.map { it to mutableStateOf(it.value) }
					}
					
					BottomSheetDialog({ showDialog = false }) {
						Column(Modifier.padding(top = 32.dp)) {
							Text(
								modifier = Modifier.padding(horizontal = 32.dp),
								style = MaterialTheme.typography.titleLarge,
								text = filter.name
							)

							FilterGroup(
								modifier = Modifier
									.fillMaxWidth()
									.weight(1f, false),
								filters = items
							)
						}
					}
				}
			}
		}
	}
}

@Composable
internal fun FiltersDialog(
	filters: List<Preference<*>>,
	onApplyFilters: (List<Preference<*>>) -> Unit,
	onDismissRequest: () -> Unit
) {
	// Don't mutate the original list
	val filters = remember(filters) {
		fun Preference<*>.copy(): Preference<*> = when(this) {
			// Don't group checks so that we can access every individual
			// copy() method by using smart-cast. Else it results into infinite recursion.
			is StringPreference -> copy()
			is IntPreference -> copy()
			is BooleanPreference -> copy()
			is LongPreference -> copy()
			is TriStatePreference -> copy()
			is SelectPreference -> copy()

			// Labels doesn't have any mutable state so we can pass it as it is
			is LabelPreference -> this

			is PreferenceGroup -> copy(
				items = items.map { it.copy() }
			)
		}
		
		filters.map { it.copy() to mutableStateOf(it.value) }
	}
	
	BottomSheetDialog(onDismissRequest) {
		Column(Modifier.padding(top = 32.dp)) {
			Text(
				modifier = Modifier.padding(horizontal = 32.dp),
				style = MaterialTheme.typography.titleLarge,
				text = "Filters"
			)
			
			FilterGroup(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f, false),
				filters = filters.filter { it.first.role != Preference.Role.QUERY }
			)
			
			HorizontalDivider()

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 32.dp)
			) {
				val buttonPadding = PaddingValues(12.dp)
				
				TextButton(
					modifier = Modifier.weight(1f),
					contentPadding = buttonPadding,
					onClick = {
						onApplyFilters(filters.map { it.first })
						onDismissRequest()
					}
				) {
					Icon(
						modifier = Modifier.size(22.dp),
						painter = painterResource(Res.drawable.ic_done),
						contentDescription = null
					)

					Text(
						modifier = Modifier.padding(horizontal = 8.dp),
						text = stringResource(Res.string.apply)
					)
				}

				TextButton(
					modifier = Modifier.weight(1f),
					contentPadding = buttonPadding,
					onClick = onDismissRequest
				) {
					Icon(
						modifier = Modifier.size(22.dp),
						painter = painterResource(Res.drawable.ic_close),
						contentDescription = null
					)

					Text(
						modifier = Modifier.padding(horizontal = 8.dp),
						text = stringResource(Res.string.cancel)
					)
				}
			}
		}
	}
}