package com.mrboomdev.awery.ui.activity.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.items
import androidx.tv.material3.Card
import androidx.tv.material3.Carousel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.mrboomdev.awery.app.AweryLifecycle.Companion.appContext
import com.mrboomdev.awery.ext.data.CatalogMedia

class TvMainActivity : ComponentActivity() {

	companion object {
		private const val THUMBNAIL_URL = "https://i.ibb.co/QD0b4HD/5liphf3577971.jpg"
		private const val BANNER_URL = "https://i.ibb.co/YNDxjs7/b40jj2sfqfpd1.png"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			MaterialTheme {
				CatalogBrowser(
					featuredContent = listOf(
						CatalogMedia(
							globalId = "0",
							titles = arrayOf("Attack on Titan"),
							banner = BANNER_URL,
							poster = THUMBNAIL_URL
						),

						CatalogMedia(
							globalId = "0",
							titles = arrayOf("FNaF Movie"),
							banner = BANNER_URL,
							poster = THUMBNAIL_URL
						)
					),

					sectionList = listOf(
						Section(
							title = "Best Movies",
							items = listOf(
								CatalogMedia(
									globalId = "0",
									titles = arrayOf("Jujustsu Kaisen"),
									banner = BANNER_URL,
									poster = THUMBNAIL_URL
								),

								CatalogMedia(
									globalId = "0",
									titles = arrayOf("Minecraft Movie"),
									banner = BANNER_URL,
									poster = THUMBNAIL_URL
								)
							)
						),

						Section(
							title = "Best Movies",
							items = listOf(
								CatalogMedia(
									globalId = "0",
									titles = arrayOf("Jujustsu Kaisen"),
									banner = BANNER_URL,
									poster = THUMBNAIL_URL
								),

								CatalogMedia(
									globalId = "0",
									titles = arrayOf("Minecraft Movie"),
									banner = BANNER_URL,
									poster = THUMBNAIL_URL
								)
							)
						)
					)
				)
			}
		}
	}

	data class Section(
		val title: String,
		val items: List<CatalogMedia>
	)

	@Composable
	fun CatalogBrowser(
		featuredContent: List<CatalogMedia>,
		sectionList: List<Section>,
		modifier: Modifier = Modifier,
		onItemSelected: (CatalogMedia) -> Unit = {}
	) {
		TvLazyColumn(
			modifier = modifier.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			item {
				FeaturedCarousel(featuredContent)
			}

			items(sectionList) { section ->
				Section(section, onItemSelected = onItemSelected)
			}
		}
	}

	@Composable
	fun Section(
		section: Section,
		modifier: Modifier = Modifier,
		onItemSelected: (CatalogMedia) -> Unit = {}
	) {
		Text(
			text = section.title,
			style = MaterialTheme.typography.headlineSmall,
			color = Color(0xFFFFFFFF),
			modifier = Modifier.padding(8.dp)
		)

		LazyRow(
			modifier = modifier,
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			items(section.items) { media ->
				MovieCard(
					media = media,
					onClick = { onItemSelected(media) }
				)
			}
		}
	}

	@Composable
	fun MovieCard(
		media: CatalogMedia,
		modifier: Modifier = Modifier,
		onClick: () -> Unit = {}
	) {
		Card(
			modifier = modifier
				.width(175.dp)
				.height(250.dp),
			onClick = onClick
		) {
			AsyncImage(
				model = media.poster,
				contentDescription = media.title,
				contentScale = ContentScale.Crop
			)
		}
	}

	@OptIn(ExperimentalTvMaterial3Api::class)
	@Composable
	fun FeaturedCarousel(
		featuredContent: List<CatalogMedia>,
		modifier: Modifier = Modifier
	) {
		Carousel(
			itemCount = featuredContent.size,
			modifier = modifier
				.height((appContext.resources.configuration.screenHeightDp - 50).dp)
		) { index ->
			val media = featuredContent[index]

			Box {
				AsyncImage(
					model = media.banner,
					contentDescription = media.description,
					contentScale = ContentScale.Crop,
					modifier = Modifier.fillMaxSize()
				)

				Text(text = media.title ?: "No title")
			}
		}
	}
}