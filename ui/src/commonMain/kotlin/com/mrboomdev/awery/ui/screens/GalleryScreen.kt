package com.mrboomdev.awery.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_close
import com.mrboomdev.awery.resources.ic_download
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.components.toast
import com.mrboomdev.awery.ui.utils.classify
import com.mrboomdev.awery.ui.utils.niceSideInset
import com.mrboomdev.awery.ui.utils.padding
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.saveImageToGallery
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import me.saket.telephoto.ExperimentalTelephotoApi
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import okio.FileSystem
import okio.SYSTEM
import org.jetbrains.compose.resources.painterResource
import kotlin.coroutines.cancellation.CancellationException

private sealed interface GalleryElementState {
	data object Loading: GalleryElementState
	data class Loaded(val width: Int, val height: Int, val diskCacheKey: String?): GalleryElementState
	data class Error(val throwable: Throwable): GalleryElementState
}

@OptIn(ExperimentalTelephotoApi::class)
@Composable
fun GalleryScreen(
	onDismissRequest: () -> Unit,
	elements: List<String>
) {
	var showUi by rememberSaveable { mutableStateOf(true) }
	
	HorizontalPager(
		modifier = Modifier.fillMaxSize(),
		state = rememberPagerState { elements.size },
		key = { elements[it] }
	) { index ->
		val context = LocalPlatformContext.current
		val focusRequester = remember { FocusRequester() }
		var state by remember(elements, index) { mutableStateOf<GalleryElementState>(GalleryElementState.Loading) }

		val zoomableState = rememberZoomableState(
			zoomSpec = ZoomSpec(maxZoomFactor = 10f)
		)
		
		val model = remember(elements, index) {
			ImageRequest.Builder(context)
				.data(elements[index])
				.memoryCacheKey(elements[index])
				.placeholderMemoryCacheKey(elements[index])
				.crossfade(1000)
				.listener(
					onStart = {
						state = GalleryElementState.Loading
					},
					
					onSuccess = { _, result -> 
						state = GalleryElementState.Loaded(
							result.image.width, 
							result.image.height, 
							result.diskCacheKey
						)
						
//						zoomableState.setContentLocation(
//							ZoomableContentLocation.scaledInsideAndCenterAligned(
//								result.image.asPainter(context).intrinsicSize
//							)
//						)
					},
					
					onCancel = { _ -> 
						state = GalleryElementState.Error(CancellationException())
					},
					
					onError = { _, error ->
						state = GalleryElementState.Error(error.throwable) 
					}
				).build()
		}

		LaunchedEffect(Unit) {
			focusRequester.requestFocus()
		}
		
		Box(
			modifier = Modifier.fillMaxSize(),
			contentAlignment = Alignment.Center
		) {
			AsyncImage(
				modifier = Modifier
					.fillMaxSize()
					.focusRequester(focusRequester)
					.zoomable(
						state = zoomableState,
						onClick = { showUi = !showUi },
						onLongClick = { showUi = !showUi }
					),
				
				model = model,
				contentDescription = null
			)

			AnimatedVisibility(
				modifier = Modifier
					.fillMaxWidth()
					.align(Alignment.TopStart),
				enter = slideInVertically(initialOffsetY = { -it }),
				exit = slideOutVertically(targetOffsetY = { -it }),
				visible = showUi
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = niceSideInset()),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					IconButton(
						painter = painterResource(Res.drawable.ic_close),
						contentDescription = null,
						onClick = onDismissRequest
					)

					(state as? GalleryElementState.Loaded)?.diskCacheKey?.also { diskCacheKey ->
						val toaster = LocalToaster.current

						IconButton(
							painter = painterResource(Res.drawable.ic_download),
							contentDescription = null,
							onClick = {
								runBlocking {
									try {
										SingletonImageLoader.get(context).diskCache!!.openSnapshot(diskCacheKey)!!.use {
											FileKit.saveImageToGallery(
												bytes = FileSystem.SYSTEM.read(it.data) { readByteArray() },
												filename = buildString {
													val url = Url(elements[index]).encodedPathAndQuery
													append(url)

													append(when(url.substringAfterLast(".")) {
														"png", "jpg", "jpeg", "gif", "webp" -> ""
														else -> ".png"
													})
												}
											)
										}

										toaster.toast("Downloaded successfully!")
									} catch(t: Throwable) {
										toaster.toast(
											title = "Failed to download!",
											message = t.classify().message
										)
									}
								}
							}
						)
					}
				}
			}

			Crossfade(state) { state ->
				when(state) {
					GalleryElementState.Loading -> {
						CircularProgressIndicator()
					}

					is GalleryElementState.Error -> {
						Text(state.throwable.classify().message)
					}
					
					is GalleryElementState.Loaded -> {}
				}
			}
		}
	}
}