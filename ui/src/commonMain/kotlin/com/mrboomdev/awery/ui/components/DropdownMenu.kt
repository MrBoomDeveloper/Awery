package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun DropdownMenu(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	content: @Composable ColumnScope.() -> Unit
) {
	DropdownMenu(
		expanded = expanded,
		onDismissRequest = onDismissRequest,
		shape = RoundedCornerShape(16.dp),
		containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
		tonalElevation = 4.dp,
		shadowElevation = 4.dp,
		content = content
	)
}

@Composable
fun DropdownMenuItem(
	text: @Composable () -> Unit,
	onClick: () -> Unit
) {
	DropdownMenuItem(
		text = text,
		onClick = onClick,
		contentPadding = PaddingValues(horizontal = 24.dp)
	)
}