package com.mrboomdev.awery.ui.tv.components

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.theme.TvTheme
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.mobile.dialogs.MediaActionsDialog

/**
 * An media card with components optimized for TV.
 */
@Composable
fun MediaCard(
	media: CatalogMedia,
	modifier: Modifier = Modifier,
	onClick: () -> Unit = {}
) {
	Column(modifier = modifier) {
		Card(
			onClick = onClick,
			onLongClick = { MediaActionsDialog(media).show(getAnyActivity<Activity>()!!) },
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
		) {
			Box(modifier = Modifier.fillMaxSize()) {
				AsyncImage(
					model = media.poster,
					contentDescription = media.title,
					contentScale = ContentScale.Crop,
					modifier = Modifier
						.fillMaxSize()
						.background(Color(0xFF111111))
				)
			}
		}

		if(media.title != null) {
			Text(
				color = Color.White,
				text = media.title!!,
				fontSize = 16.sp,
				maxLines = 2,
				minLines = 2,

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

	TvTheme(
		palette = AwerySettings.ThemeColorPaletteValue.RED,
		isDark = true,
		isAmoled = false
	) {
		MediaCard(
			media = media,
			modifier = Modifier.width(175.dp).height(250.dp)
		)
	}
}