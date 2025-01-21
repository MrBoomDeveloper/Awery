package com.mrboomdev.awery.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mrboomdev.awery.app.theme.LocalAweryTheme

interface DialogScope {
	fun requestDismiss()
}

@Composable
fun MaterialDialog(
	modifier: Modifier = Modifier,
	onDismissRequest: () -> Unit,
	title: (@Composable DialogScope.() -> Unit)? = null,
	dismissButton: (@Composable DialogScope.() -> Unit)? = null,
	confirmButton: @Composable DialogScope.() -> Unit,
	content: (@Composable DialogScope.() -> Unit)? = null
) {
	val scope by remember { derivedStateOf {
		object : DialogScope {
			override fun requestDismiss() = onDismissRequest()
		}
	}}
	
	AlertDialog(
		modifier = modifier,
		onDismissRequest = onDismissRequest,
		title = if(title != null) {{ title(scope)}} else null,
		text = if(content != null) {{ content(scope)}} else null,
		dismissButton = if(dismissButton != null) {{ dismissButton(scope)}} else null,
		confirmButton = { confirmButton(scope) },
		
		containerColor = if(LocalAweryTheme.current.isAmoled) {
			Color(0xFF111111)
		} else AlertDialogDefaults.containerColor
	)
}
