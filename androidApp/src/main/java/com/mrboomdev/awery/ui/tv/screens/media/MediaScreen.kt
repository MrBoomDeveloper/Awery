package com.mrboomdev.awery.ui.tv.screens.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.AssistChip
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.SubcomposeAsyncImage
import com.mrboomdev.awery.AweryDebug
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.theme.TvTheme
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogTag
import com.mrboomdev.awery.generated.*
import org.jetbrains.compose.resources.stringResource

private val SHADOW_COLOR = Color(0xBB000000)

@OptIn(ExperimentalLayoutApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun MediaScreen(media: CatalogMedia) {
	Box(modifier = Modifier
		.fillMaxSize()
		.drawBehind {
			drawRect(
				Brush.linearGradient(
					.2f to SHADOW_COLOR,
					.6f to Color.Transparent
				)
			)
		}
	) {
		SubcomposeAsyncImage(
			modifier = Modifier.fillMaxSize(),
			model = media.banner ?: media.poster,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			error = {
				if(LocalInspectionMode.current) {
					Image(
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop,
						painter = painterResource(AweryDebug.R.drawable.sample_banner),
						contentDescription = null
					)
				}
			}
		)

		LazyColumn(modifier = Modifier
			.fillMaxSize()
		) {
			item {
				MediaInfoScreen(
					media = media,
					maxTextWidth = 350.dp,
					modifier = Modifier
						.drawBehind {
							drawRect(
								Brush.verticalGradient(
									.5f to Color.Transparent,
									1f to SHADOW_COLOR
								)
							)
						}
						.fillMaxWidth()
						.padding(64.dp),
					onAction = { when(it) {
						MediaInfoAction.WATCH -> {
							toast("Coming soon...")
						}
					}}
				)
			}

			if(media.genres != null) {
				item {
					Text(
						modifier = Modifier.background(SHADOW_COLOR)
							.padding(64.dp, 8.dp)
							.fillMaxWidth(),
						style = MaterialTheme.typography.titleLarge,
						color = Color.White,
						text = stringResource(Res.string.genres)
					)
				}

				item {
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalArrangement = Arrangement.spacedBy(16.dp),
						modifier = Modifier
							.background(SHADOW_COLOR)
							.padding(64.dp, 16.dp)
							.padding(bottom = 16.dp)
							.fillMaxWidth()
					) {
						for(genre in media.genres!!) {
							AssistChip(
								onClick = { /*TODO*/ }
							) {
								Text(text = genre)
							}
						}
					}
				}
			}

			if(media.tags != null) {
				item {
					Text(
						modifier = Modifier.background(SHADOW_COLOR)
							.padding(64.dp, 8.dp)
							.fillMaxWidth(),
						style = MaterialTheme.typography.titleLarge,
						color = Color.White,
						text = stringResource(Res.string.tags)
					)
				}

				item {
					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(12.dp),
						verticalArrangement = Arrangement.spacedBy(16.dp),
						modifier = Modifier
							.background(SHADOW_COLOR)
							.padding(64.dp, 16.dp)
							.padding(bottom = 16.dp)
							.fillMaxWidth()
					) {
						for(tag in media.tags!!) {
							AssistChip(
								onClick = { /*TODO*/ }
							) {
								Text(text = tag.name)
							}
						}
					}
				}
			}
		}
	}
}

@Composable
@Preview(
	showBackground = true,
	device = "id:tv_720p"
)
fun MediaScreenPreview() {
	TvTheme(
		palette = AwerySettings.ThemeColorPaletteValue.RED,
		isDark = true,
		isAmoled = false
	) {
		MediaScreen(CatalogMedia(
			globalId = "MANAGER_ID;;;SOURCE_ID;;;MEDIA_ID",
			description = "Lorem ipsum blah blah blah... ".repeat(10),
			titles = arrayOf("Chainsaw Man"),
			tags = mutableListOf<CatalogTag>().apply {
				for(i in 1..15) {
					add(CatalogTag(name = "Tag #$i"))
				}
			}.toTypedArray(),
			genres = mutableListOf<String>().apply {
				for(i in 1..4) {
					add("Genre #$i")
				}
			}.toTypedArray()
		))
	}
}