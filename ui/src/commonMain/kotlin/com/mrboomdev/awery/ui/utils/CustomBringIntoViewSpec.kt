package com.mrboomdev.awery.ui.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.abs

open class CustomBringIntoViewSpec(
	private val parentFraction: Float,
	private val childFraction: Float,
) : BringIntoViewSpec {
	override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float {
		val leadingEdgeOfItemRequestingFocus = offset
		val trailingEdgeOfItemRequestingFocus = offset + size

		val sizeOfItemRequestingFocus =
			abs(trailingEdgeOfItemRequestingFocus - leadingEdgeOfItemRequestingFocus)
		val childSmallerThanParent = sizeOfItemRequestingFocus <= containerSize
		val initialTargetForLeadingEdge =
			(parentFraction * containerSize) -
					(childFraction * sizeOfItemRequestingFocus)
		val spaceAvailableToShowItem = containerSize - initialTargetForLeadingEdge

		val targetForLeadingEdge =
			if (childSmallerThanParent && spaceAvailableToShowItem < sizeOfItemRequestingFocus) {
				containerSize - sizeOfItemRequestingFocus
			} else {
				initialTargetForLeadingEdge
			}

		return leadingEdgeOfItemRequestingFocus - targetForLeadingEdge
	}
}