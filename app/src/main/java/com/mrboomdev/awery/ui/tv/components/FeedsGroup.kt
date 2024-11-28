package com.mrboomdev.awery.ui.tv.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.util.exceptions.ZeroResultsException
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedsGroup(
	sections: List<CatalogFeed.Loaded>? = null,
	failedSections: List<CatalogFeed.Loaded>? = null,
	onItemSelected: (media: CatalogMedia) -> Unit,
	listState: LazyListState = rememberLazyListState(),
	isLoading: Boolean = false
) {
	// a bring into view spec that pivots around the center of the scrollable container
	val customBringIntoViewSpec = object : BringIntoViewSpec {
		val customAnimationSpec = tween<Float>(easing = LinearEasing)

		override val scrollAnimationSpec: AnimationSpec<Float>
			get() = customAnimationSpec

		override fun calculateScrollDistance(
			offset: Float,
			size: Float,
			containerSize: Float
		): Float {
			val trailingEdgeOfItemRequestingFocus = offset + size

			val sizeOfItemRequestingFocus = abs(trailingEdgeOfItemRequestingFocus - offset)
			val isChildSmallerThanParent = sizeOfItemRequestingFocus <= containerSize
			val initialTargetForLeadingEdge = containerSize / 2f - (sizeOfItemRequestingFocus / 2f)
			val spaceAvailableToShowItem = containerSize - initialTargetForLeadingEdge

			val targetForLeadingEdge = if(isChildSmallerThanParent && spaceAvailableToShowItem < sizeOfItemRequestingFocus) {
				containerSize - sizeOfItemRequestingFocus
			} else {
				initialTargetForLeadingEdge
			}

			return offset - targetForLeadingEdge
		}
	}

	// LocalBringIntoViewSpec will apply to all scrollables in the hierarchy.
	CompositionLocalProvider(LocalBringIntoViewSpec provides customBringIntoViewSpec) {
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = PaddingValues(0.dp, 0.dp, 0.dp, 32.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp),
			state = listState
		) {
			if(!isLoading && sections.isNullOrEmpty() && (failedSections.isNullOrEmpty() || failedSections.find { !it.feed.hideIfEmpty } == null)) {
				item("empty") {
					Column(
						modifier = Modifier.fillMaxWidth()
							.height(LocalConfiguration.current.screenHeightDp.dp)
							.padding(bottom = 32.dp),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					) {
						Text(
							style = MaterialTheme.typography.headlineMedium,
							textAlign = TextAlign.Center,
							fontWeight = FontWeight.Bold,
							color = Color.White,
							text = "There is nothing"
						)

						Spacer(Modifier.height(15.dp))

						Text(
							modifier = Modifier.widthIn(max = 300.dp),
							style = MaterialTheme.typography.bodyMedium,
							textAlign = TextAlign.Center,
							color = Color.White,
							text = "Oh... This page is so empty. Maybe try discovering new content and adding it here?"
						)
					}
				}
			}

			if(sections != null) {
				itemsIndexed(
					items = sections,
					key = { _, item -> item.feed }
				) { index, loaded ->
					if(loaded.items.isNullOrEmpty() && loaded.feed.hideIfEmpty) {
						return@itemsIndexed
					}

					when(loaded.feed.style) {
						CatalogFeed.Style.SLIDER -> FeaturedMediaCarousel(
							content = loaded.items!!.toList(),
							modifier = Modifier.height((LocalConfiguration.current.screenHeightDp - 50).dp),
							onItemSelected = onItemSelected
						)

						CatalogFeed.Style.GRID -> TODO("PLEASE DON'T FORGET TO IMPLEMENT AN GRID STYLE!")

						CatalogFeed.Style.UNSPECIFIED -> {
							if(index == 0 && loaded.feed.filters?.get("first_large")?.value as? Boolean == true) {
								FeaturedMediaCarousel(
									content = loaded.items!!.toList(),
									modifier = Modifier.height((LocalConfiguration.current.screenHeightDp - 50).dp),
									onItemSelected = onItemSelected
								)
							} else {
								MediaRow(
									content = loaded,
									onItemSelected = onItemSelected
								)
							}
						}

						else -> MediaRow(
							content = loaded,
							onItemSelected = onItemSelected
						)
					}
				}
			}

			if(isLoading) {
				item("isLoading") {
					Row(
						modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 48.dp),
						horizontalArrangement = Arrangement.Center
					) {
						CircularProgressIndicator()
					}
				}
			}

			if(!failedSections.isNullOrEmpty()) {
				items(
					items = failedSections,
					key = { it.feed }
				) {
					if(it.feed.hideIfEmpty && (it.throwable == null || it.throwable is ZeroResultsException)) {
						return@items
					}

					MediaRow(
						content = it,
						onItemSelected = onItemSelected
					)
				}
			}
		}
	}
}