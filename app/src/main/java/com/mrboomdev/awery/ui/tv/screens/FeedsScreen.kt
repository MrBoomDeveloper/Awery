package com.mrboomdev.awery.ui.tv.screens

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.app.App.Companion.configuration
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ui.tv.components.FeaturedMediaCarousel
import com.mrboomdev.awery.ui.tv.components.MediaRow
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedsScreen(
	sections: List<CatalogFeed.Loaded>? = null,
	featuredItems: List<CatalogMedia>? = null,
	onItemSelected: (media: CatalogMedia) -> Unit,
	listState: LazyListState = rememberLazyListState()
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

			val sizeOfItemRequestingFocus =
				abs(trailingEdgeOfItemRequestingFocus - offset)
			val childSmallerThanParent = sizeOfItemRequestingFocus <= containerSize
			val initialTargetForLeadingEdge =
				containerSize / 2f - (sizeOfItemRequestingFocus / 2f)
			val spaceAvailableToShowItem = containerSize - initialTargetForLeadingEdge

			val targetForLeadingEdge =
				if (childSmallerThanParent &&
					spaceAvailableToShowItem < sizeOfItemRequestingFocus
				) {
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
			if(featuredItems != null) {
				item {
					FeaturedMediaCarousel(
						featuredContent = featuredItems,
						modifier = Modifier.height((configuration.screenHeightDp - 50).dp),
						onItemSelected = onItemSelected
					)
				}
			}
			if(sections != null) {
				items(sections) {
					MediaRow(
						content = it,
						onItemSelected = onItemSelected
					)
				}
			}
		}
	}
}