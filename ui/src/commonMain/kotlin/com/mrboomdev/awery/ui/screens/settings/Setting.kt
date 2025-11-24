package com.mrboomdev.awery.ui.screens.settings

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.data.settings.*
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.cancel
import com.mrboomdev.awery.resources.ok
import com.mrboomdev.awery.ui.components.AlertDialog
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource

@Composable
fun Setting(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    range: IntRange? = null,
    step: Int = 1,
    setting: Setting<*>,
    enumValues: List<Pair<Enum<*>, String>>? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    var showDialog by remember(setting) { mutableStateOf(false) }

    if(showDialog) {
        var newValue by remember(setting) { mutableStateOf(setting.value!!) }
        
        fun save() {
            if(setting.value != newValue) {
				launchGlobal(Dispatchers.Default) {
					setting.let {
						@Suppress("UNCHECKED_CAST")
						it as Setting<Any>
					}.set(newValue)
				}
            }

            showDialog = false
        }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            contentPadding = PaddingValues(vertical = 6.dp),
            title = title,

            confirmButton = {
                TextButton(onClick = ::save) {
                    Text(stringResource(Res.string.ok))
                }
            },

            cancelButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            when(setting) {
                is EnumSetting<*> -> {
                    LazyColumn(Modifier.fillMaxWidth()) {
                        requireNotNull(enumValues) {
                            "Setting.enumValues must be not null for EnumSetting!"
                        }

                        items(
                            items = enumValues,
                            key = { it.first.name }
                        ) { (value, name) ->
                            val interactionSource = remember { MutableInteractionSource() }

                            Row(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = LocalIndication.current,
                                        role = Role.RadioButton,
                                        onClick = { newValue = value }
                                    )
                                    .fillMaxWidth()
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

                is IntSetting -> {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        text = newValue.toString()
                    )

                    Slider(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        value = (newValue as Int).toFloat(),
                        onValueChange = { newValue = it.toInt() },
                        steps = step,
                        valueRange = range!!.let { it.first.toFloat()..it.last.toFloat() }
                    )
                }
                
                is StringSetting -> {
                    val focusRequester = remember { FocusRequester() }
                    
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }

                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        
                        value = newValue as String,
                        onValueChange = { newValue = it },
                        singleLine = true,
                        
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        
                        keyboardActions = KeyboardActions(
                            onDone = { save() }
                        )
                    )
                }

                else -> throw UnsupportedOperationException(
                    "Setting type ${setting::class.simpleName} isn't implemented yet!"
                )
            }
        }
    }

    Setting(
        modifier = modifier
            .then(when(setting) {
                is BooleanSetting -> Modifier.toggleable(
                    onValueChange = { launchGlobal(Dispatchers.Default) { setting.set(it) } },
                    value = setting.collectAsState().value,
                    indication = LocalIndication.current,
                    interactionSource = interactionSource,
                    role = Role.Switch
                )

                is StringSetting, is IntSetting, is LongSetting, is EnumSetting<*> -> Modifier.clickable(
                    role = Role.Button,
                    onClick = { showDialog = true }
                )
                
                is SerializableSetting<*> -> {
                    throw UnsupportedOperationException("SerializableSetting cannot be rendered correctly!")
                }
            }),

        contentPadding = contentPadding,
        icon = icon,
        title = title,
        description = description
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            when(setting) {
                is BooleanSetting -> {
                    Switch(
                        interactionSource = interactionSource,
                        checked = setting.collectAsState().value,
                        onCheckedChange = { launchGlobal(Dispatchers.Default) { setting.set(it) } }
                    )
                }

                is IntSetting, is LongSetting, is StringSetting -> {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        text = setting.collectAsState().value.toString()
                    )
                }

                is EnumSetting<*> -> {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        text = enumValues!!.first { it.first == setting.collectAsState().value }.second
                    )
                }
                
                is SerializableSetting<*> -> {
                    throw UnsupportedOperationException("SerializableSetting cannot be rendered!")
                }
            }
        }
    }
}

@Composable
fun Setting(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    description: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .padding(contentPadding)
            .defaultMinSize(minHeight = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.also {
            Column {
                it()
                Spacer(Modifier.height(2.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge
                    .copy(color = MaterialTheme.colorScheme.onSurface)
            ) {
                title()
            }

            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium
                    .copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                description?.invoke()
            }
        }

        content?.invoke()
    }
}