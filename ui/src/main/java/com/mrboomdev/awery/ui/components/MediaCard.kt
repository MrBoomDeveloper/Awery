package com.mrboomdev.awery.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrboomdev.awery.ext.data.CatalogMedia

@Composable
fun MediaCard(
	media: CatalogMedia,
	modifier: Modifier = Modifier,
	onClick: () -> Unit = {}
) {
	Column(
		modifier = modifier
			.clickable { onClick() }
	) {
		/*Card(
			onClick = onClick,
			modifier = modifier
				.width(175.dp)
				.height(250.dp)
		) {
			Box {
				AsyncImage(
					model = media.poster,
					contentDescription = media.title,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.background(Color(0xFF1D1D1D))
				)
			}
		}*/

		if(media.title != null) {
			Text(
				color = Color.White,
				text = media.title!!,
				fontSize = 16.sp,

				style = TextStyle(
					shadow = Shadow(
						color = Color.Black,
						blurRadius = 4f
					)
				),

				modifier = Modifier
					.padding(8.dp, 16.dp, 8.dp, 0.dp)
			)
		}
	}
}