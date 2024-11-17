package com.mrboomdev.awery.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.app.theme.TvTheme

/**
 * An media card with components optimized for TV.
 */
@Composable
fun MediaCard(
	media: CatalogMedia,
	modifier: Modifier = Modifier,
	onClick: () -> Unit = {}
) {
	Column {
		Card(
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
		}

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

@Preview(showBackground = true)
@Composable
fun MediaCardPreview() {
	val media = CatalogMedia(
		globalId = "a;;;a;;;a",
		titles = arrayOf("Attack on Titan")
	)

	TvTheme {
		MediaCard(
			media = media,
			modifier = Modifier.width(175.dp).height(250.dp)
		)
	}
}