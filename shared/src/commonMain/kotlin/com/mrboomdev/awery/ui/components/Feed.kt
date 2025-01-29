package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.ui.utils.update

@Composable
fun FeedHeader(
	modifier: Modifier = Modifier,
	title: @Composable () -> Unit,
	onClick: (() -> Unit)? = null,
	actionButton: (@Composable () -> Unit)? = null
) {
	Row(
		modifier = modifier.update { onClick?.let { clickable { it() } } },
		verticalAlignment = Alignment.CenterVertically
	) { 
		title()
		Spacer(Modifier.weight(1f))
		actionButton?.let { it() }
	}
}