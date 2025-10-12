package com.mrboomdev.awery.ui.screens.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewModelScope
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.Platform
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.core.utils.collection.replace
import com.mrboomdev.awery.core.utils.launchGlobal
import com.mrboomdev.awery.core.utils.launchTrying
import com.mrboomdev.awery.core.utils.toCalendar
import com.mrboomdev.awery.data.database.database
import com.mrboomdev.awery.data.database.entity.DBWatchProgress
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.extension.sdk.WatchVariant
import com.mrboomdev.awery.extension.sdk.get
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.AlertDialog
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.popups.BookmarkMediaDialog
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.awery.ui.utils.formatAsCountry
import com.mrboomdev.awery.ui.utils.thenIf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ColumnScope.MediaScreenActions(
    destination: Routes.Media,
    viewModel: MediaScreenViewModel,
    alignAtCenter: Boolean,
    stretchButtons: Boolean,
    onWatch: () -> Unit
) {
    val navigation = Navigation.current()
    val toaster = LocalToaster.current 
	val windowSize = currentWindowSize() 
	val media by viewModel.media.collectAsState()

    var showEpisodesDialog by remember { mutableStateOf(false) }
    var showBookmarkDialog by remember { mutableStateOf(false) }

    SelectionContainer {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = if(alignAtCenter) TextAlign.Center else TextAlign.Start,
            maxLines = 5,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            text = media.title
        )
    }

    listOfNotNull(
        destination.extensionName,
        media.ageRating,
        media.releaseDate?.takeIf { it > 0L }?.toCalendar()?.get(Calendar.YEAR),
        media.country?.formatAsCountry(),

        media.episodes?.let { episodes ->
            pluralStringResource(when(media.type) {
                Media.Type.WATCHABLE -> Res.plurals.episodes
                Media.Type.READABLE -> Res.plurals.chapters
            }, episodes, episodes)
        },

        media.tags?.takeIf { it.isNotEmpty() }?.joinToString(", ")
    ).joinToString(" • ").takeIf { it.isNotBlank() }?.also { meta ->
        Spacer(Modifier.height(8.dp))

        SelectionContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .thenIf(alignAtCenter) { padding(horizontal = 16.dp) },
                textAlign = if(alignAtCenter) TextAlign.Center else TextAlign.Start,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                text = meta
            )
        }
    }

    Spacer(Modifier.height(12.dp))

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Button(
			modifier = Modifier.thenIf(stretchButtons) { weight(1f) },
			onClick = onWatch
		) {
			Icon(
				modifier = Modifier.size(26.dp),
				contentDescription = null,
				painter = painterResource(when(media.type) {
					Media.Type.WATCHABLE -> Res.drawable.ic_play_filled
					Media.Type.READABLE -> Res.drawable.ic_book_filled
				})
			)

			Text(
				modifier = Modifier.padding(start = 8.dp, end = 12.dp),
				text = stringResource(when(media.type) {
					Media.Type.WATCHABLE -> Res.string.watch_now
					Media.Type.READABLE -> Res.string.read_now
				})
			)
		}

		FilledTonalButton(
			modifier = Modifier.thenIf(stretchButtons) { weight(1f) },
			onClick = { showBookmarkDialog = true }
		) {
			Icon(
				modifier = Modifier.size(22.dp),
				painter = painterResource(Res.drawable.ic_bookmark_filled),
				contentDescription = null
			)

			Text(
				modifier = Modifier.padding(start = 8.dp, end = 8.dp),
				text = stringResource(Res.string.bookmark)
			)
		}
		
		media.url?.also { url ->
			if(Awery.platform != Platform.DESKTOP) return@also
			val toaster = LocalToaster.current
			
			FilledTonalIconButton(
				onClick = {
					Awery.copyToClipboard(url)
					toaster.toast("Link copied to the clipboard!")
				}
			) {
				Icon(
					modifier = Modifier.size(22.dp),
					painter = painterResource(Res.drawable.ic_link),
					contentDescription = null
				)
			}
		}
	}

    if(showBookmarkDialog) {
        BookmarkMediaDialog(
			extensionId = destination.extensionId,
			media = media
		) { showBookmarkDialog = false }
	}

    if(showEpisodesDialog) {
        val watchVariants = remember { mutableStateListOf<Pair<WatchVariant, DBWatchProgress?>>() }
        var module by remember { mutableStateOf<WatchModule?>(null) }
        var job by remember { mutableStateOf<Job?>(null) }
        var episode by remember { mutableStateOf<WatchVariant?>(null) }

        fun cancelJob() {
            job?.cancel()
            job = null
            episode = null
            module = null
            watchVariants.clear()
            showEpisodesDialog = false
        }

        fun watch(video: Video) {
            viewModel.viewModelScope.launch(Dispatchers.Main) {
                navigation.push(
                    Routes.Player(
                    video = video,
                    title = episode?.title ?: video.title ?: video.url
                ))
            }
        }

        fun selectVariant(variant: WatchVariant) {
            watchVariants.clear()

            if(variant.type == WatchVariant.Type.EPISODE) {
                episode = variant
            }

            job = viewModel.viewModelScope.launchTrying(Dispatchers.Default, onCatch = {
                cancelJob()

                if(it is CancellationException) {
                    return@launchTrying
                }

                Log.e("MediaScreen", "Failed to get media video!", it)
                toaster.toast("Failed to play an episode!")
            }) {
                module!!.watch(variant, 0).get(::watch) { variants ->
                    if(variants.items.size == 1) {
                        selectVariant(variants.items[0])
                        return@get
                    }

                    watchVariants.clear()

                    watchVariants.addAll(variants.items.map {
                        it to Awery.database.progress.get(
                            destination.extensionId,
                            destination.media.id,
                            it.id
                        )
                    })
                }
            }
        }

        LaunchedEffect(Unit) {
            job = viewModel.viewModelScope.launch(Dispatchers.Default) {
                module = Extensions[destination.extensionId].also {
                    if(it == null) {
                        toaster.toast("Source extension isn't installed!")
                        showEpisodesDialog = false
                        return@launch
                    }
                }?.get<WatchModule>() ?: run {
                    toaster.toast("Video unavailable!")
                    showEpisodesDialog = false
                    return@launch
                }

                try {
                    module!!.watch(media, 0).get(::watch) { variants ->
                        watchVariants.clear()
                        watchVariants.addAll(variants.items.map {
                            it to Awery.database.progress.get(
                                destination.extensionId,
                                destination.media.id,
                                it.id
                            )
                        })
                    }
                } catch(t: Throwable) {
                    cancelJob()

                    if(t is CancellationException) {
                        return@launch
                    }

                    Log.e("MediaScreen", "Failed to get media video!", t)
                    toaster.toast("Failed to get episodes!")
                }
            }
        }

        if(watchVariants.isEmpty()) {
            Dialog(onDismissRequest = { cancelJob() }) {
                CircularProgressIndicator()
            }
        } else {
            AlertDialog(
                contentPadding = PaddingValues.Zero,
                onDismissRequest = { cancelJob() }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    items(
                        items = watchVariants,
                        key = { it.first.id }
                    ) {
                        val (variant, progress) = it

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .thenIf(run {
                                    val value = progress?.progress ?: 0
                                    (value > 0 || value == -1L) &&
                                            (variant.type != WatchVariant.Type.QUALITY &&
                                                    variant.type != WatchVariant.Type.LOCALE)
                                }) { alpha(.5f) }
                                .clickable {
                                    if(progress == null) {
                                        val newProgress = DBWatchProgress(
                                            extensionId = destination.extensionId,
                                            mediaId = destination.media.id,
                                            variantId = variant.id,
                                            progress = -1L,
                                            title = media.title + " " + variant.title
                                        )

                                        launchGlobal { Awery.database.progress.add(newProgress) }
                                        watchVariants.replace(it, variant to newProgress)
                                    }

                                    selectVariant(variant)
                                }
                                .heightIn(min = 48.dp)
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                color = MaterialTheme.colorScheme.onSurface,
                                text = variant.title
                            )
                        }
                    }
                }
            }
        }
    }
}