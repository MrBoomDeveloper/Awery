package com.mrboomdev.awery.ui.tv.screens.media

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.lightColorScheme
import com.mrboomdev.awery.AweryDebug
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.getAnyActivity
import com.mrboomdev.awery.app.AweryLocales
import com.mrboomdev.awery.app.ExtensionsManager
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.ui.mobile.dialogs.MediaBookmarkDialog
import com.mrboomdev.awery.ui.mobile.screens.BrowserActivity
import com.mrboomdev.awery.util.extensions.plus
import com.mrboomdev.awery.util.extensions.startActivity
import com.mrboomdev.awery.util.extensions.toCalendar
import com.mrboomdev.awery.utils.buildIntent
import org.jetbrains.compose.resources.stringResource
import java.util.Calendar

enum class MediaInfoAction {
	WATCH
}

@Composable
fun MediaInfoScreen(
	media: CatalogMedia,
	modifier: Modifier = Modifier,
	maxTextWidth: Dp = Dp.Unspecified,
	onAction: (action: MediaInfoAction) -> Unit
) {
	Column(
		modifier = modifier
	) {
		Text(
			modifier = Modifier.widthIn(max = maxTextWidth),
			text = media.title ?: stringResource(Res.string.no_title),
			maxLines = 2,
			color = Color.White,
			style = TextStyle(
				fontSize = 40.sp,
				shadow = Shadow(blurRadius = 5f)
			)
		)

		Spacer(Modifier.height(14.dp))

		Text(
			modifier = Modifier.widthIn(max = maxTextWidth),
			color = Color.White,
			style = TextStyle(
				fontSize = 18.sp,
				shadow = Shadow(blurRadius = 5f)
			),
			text = mutableListOf<String>().apply {
				if(media.episodesCount != null) {
					add(media.episodesCount + " " + stringResource(
						if(media.episodesCount == 1) Res.string.episode
						else Res.string.episodes)
					)
				}

				if(media.duration != null) {
					add(media.duration!!.let {
						return@let if(it < 60) {
							"$it${stringResource(Res.string.minute_short)}"
						} else {
							"${it / 60}${stringResource(Res.string.hour_short)} " +
									"${it % 60}${stringResource(Res.string.minute_short)}"
						}
					} + " " + stringResource(Res.string.duration))
				}

				if(media.releaseDate != null) {
					add(media.releaseDate!!.toCalendar()[Calendar.YEAR].toString())
				}

				if(media.country != null) {
					add(AweryLocales.i18nCountryName(
                        media.country!!
                    ))
				}

				if(size < 3 && media.status != null) {
					add(stringResource(when(media.status!!) {
						CatalogMedia.Status.ONGOING ->
							Res.string.status_releasing

						CatalogMedia.Status.COMPLETED ->
							Res.string.status_finished

						CatalogMedia.Status.COMING_SOON ->
							Res.string.status_not_yet_released

						CatalogMedia.Status.PAUSED ->
							Res.string.status_hiatus

						CatalogMedia.Status.CANCELLED ->
							Res.string.status_cancelled
					}))
				}

				if(size < 3) {
					add(ExtensionsManager.getSource(media.globalId)?.let {
						return@let it.context.name
					} ?: "Source not installed")
				}
			}.joinToString("  •  ")
		)

		Spacer(Modifier.height(18.dp))

		Text(
			modifier = Modifier.widthIn(max = maxTextWidth),
			overflow = TextOverflow.Ellipsis,
			maxLines = 3,
			text = media.description ?: stringResource(Res.string.no_description_available),
			color = Color.White,
			style = TextStyle(
				fontSize = 18.sp,
				shadow = Shadow(blurRadius = 5f)
			)
		)

		Spacer(Modifier.height(25.dp))

		Row {
			MaterialTheme(colorScheme = lightColorScheme()) {
				Button(onClick = {
					onAction(MediaInfoAction.WATCH)
				}) {
					Text(
						fontSize = 17.sp,
						text = when(media.type) {
							CatalogMedia.Type.BOOK, CatalogMedia.Type.POST ->
								stringResource(Res.string.read_now)

							else -> stringResource(Res.string.watch_now)
						}
					)
				}

				Spacer(Modifier.width(10.dp))

				IconButton(onClick = {
					toast("This action isn't done yet!")
				}) {
					Icon(
						modifier = Modifier.padding(8.dp),
						painter = painterResource(R.drawable.ic_sync),
						contentDescription = null
					)
				}

				Spacer(Modifier.width(10.dp))

				IconButton(onClick = {
					MediaBookmarkDialog(media).show(getAnyActivity<Activity>()!!)
				}) {
					Icon(
						modifier = Modifier.padding(8.dp),
						painter = painterResource(R.drawable.ic_bookmark_filled),
						contentDescription = stringResource(Res.string.bookmark)
					)
				}

				if(media.url != null) {
					Spacer(Modifier.width(10.dp))

					IconButton(onClick = {
						getAnyActivity<Activity>()!!.apply {
							startActivity(buildIntent(BrowserActivity::class, BrowserActivity.Extras(media.url!!)))
						}
					}) {
						Icon(
							modifier = Modifier.padding(8.dp),
							painter = painterResource(R.drawable.ic_language),
							contentDescription = null
						)
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
fun MediaInfoScreenPreview() {
	Box(modifier = Modifier.fillMaxSize()) {
		Image(
			modifier = Modifier.fillMaxSize(),
			contentScale = ContentScale.Crop,
			painter = painterResource(AweryDebug.R.drawable.sample_banner),
			contentDescription = null
		)

		MediaInfoScreen(
			onAction = {},
			media = CatalogMedia(
				globalId = "MANAGER_ID;;;SOURCE_ID;;;MEDIA_ID",
				titles = arrayOf("Attack on Titan"),
				duration = 24,
				episodesCount = 75,
				status = CatalogMedia.Status.COMPLETED,
			)
		)
	}
}