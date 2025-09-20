package com.mrboomdev.awery.ui.popups

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.DBList
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.cancel
import com.mrboomdev.awery.resources.create
import com.mrboomdev.awery.resources.create_list
import com.mrboomdev.awery.resources.list_name
import com.mrboomdev.awery.resources.text_cant_empty
import com.mrboomdev.awery.ui.components.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateListDialog(
    onDismissRequest: () -> Unit,
    onListCreated: (DBList) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    fun startTheShow() {
        isLoading = true

        coroutineScope.launch(Dispatchers.Default) {
            if(name.isBlank()) {
                error = getString(Res.string.text_cant_empty)
                isLoading = false
                return@launch
            }

            onListCreated(with(Awery.database.lists) {
                get(insert(DBList(name = name)))
            })

            onDismissRequest()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.create_list)) },

        confirmButton = {
            TextButton(
                enabled = !isLoading,
                onClick = ::startTheShow
            ) {
                Text(stringResource(Res.string.create))
            }
        },

        cancelButton = {
            TextButton(
                enabled = !isLoading,
                onClick = onDismissRequest
            ) {
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
                .padding(vertical = 16.dp)
                .focusRequester(focusRequester),

            value = name,
            enabled = !isLoading,
            isError = error != null,
            singleLine = true,

            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground
            ),

            placeholder = {
                Text(stringResource(Res.string.list_name))
            },

            supportingText = error?.let {{
                Text(
                    color = MaterialTheme.colorScheme.error,
                    text = it
                )
            }},

            onValueChange = {
                name = it
                error = null
            },

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),

            keyboardActions = KeyboardActions(
                onDone = { startTheShow() }
            )
        )
    }
}