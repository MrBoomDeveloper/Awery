package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.util.extensions.configuration
import com.mrboomdev.awery.util.extensions.screenHeight
import kotlinx.serialization.Serializable

@Composable
fun MediaScreen(
	media: CatalogMedia
) {
	val scrollState = rememberScrollState()

	Surface(
		modifier = Modifier.fillMaxSize()
	) {
		AsyncImage(
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop,
			model = media.banner,
			contentDescription = null
		)

		Box(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(scrollState)
		) {
			val screenHeight = LocalContext.current.configuration.screenHeightDp

			Column(
				modifier = Modifier.fillMaxSize()
			) {
				Spacer(modifier = Modifier.height((screenHeight / 2f).dp))

				Canvas(
					modifier = Modifier
						.fillMaxWidth()
						.height((screenHeight / 2f).dp),

					onDraw = {
						drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
					}
				)

				//Spacer(modifier = Modifier.fillMaxHeight().width(100.dp).background(Color.Red))
			}

			Column(
				modifier = Modifier
					.padding(32.dp)
			) {
				Text(
					style = MaterialTheme.typography.displayMedium,
					text = media.title ?: "No title"
				)

				Spacer(Modifier.height(10.dp))

				Text(
					style = MaterialTheme.typography.titleLarge,
					text = media.description ?: "No description"
				)

				Spacer(Modifier.height(20.dp))

				Button(onClick = {
					toast("Coming soon...")
				}) {
					Text(
						text = stringResource(R.string.watch_now),
						fontSize = 16.sp
					)
				}
			}
		}
	}
}