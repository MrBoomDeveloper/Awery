package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StateBox(
	modifier: Modifier = Modifier,
	title: String,
	message: String
) {
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			fontWeight = FontWeight.SemiBold,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.headlineMedium,
			text = title
		)
		
		Text(
			modifier = Modifier.padding(12.dp),
			textAlign = TextAlign.Center,
			text = message
		)
	}
}