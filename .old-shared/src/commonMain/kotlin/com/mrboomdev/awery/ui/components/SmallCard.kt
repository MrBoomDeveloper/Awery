package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun SmallCard(
	modifier: Modifier = Modifier
		.width(125.dp)
		.height(235.dp),
	image: String? = null,
	title: String,
	onClick: () -> Unit
) {
	Column(
		modifier = modifier
			.clip(RoundedCornerShape(8.dp))
			.clickable(onClick = onClick)
	) {
		AsyncImage(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colorScheme.surfaceBright),
			model = image,
			contentDescription = null,
			contentScale = ContentScale.Crop
		)
		
		Text(
			modifier = Modifier.padding(8.dp),
			minLines = 2,
			maxLines = 2,
			text = title
		)
	}
}