package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@get:Composable
val PaddingValues.top get() = calculateTopPadding()

@get:Composable
val PaddingValues.bottom get() = calculateBottomPadding()

@get:Composable
val PaddingValues.start get() = calculateStartPadding(LocalLayoutDirection.current)

@get:Composable
val PaddingValues.end get() = calculateEndPadding(LocalLayoutDirection.current)

@get:Composable
val PaddingValues.left get() = calculateLeftPadding(LocalLayoutDirection.current)

@get:Composable
val PaddingValues.right get() = calculateRightPadding(LocalLayoutDirection.current)

@Composable
operator fun PaddingValues.plus(padding: PaddingValues) = add(
    start = padding.start,
    top = padding.top,
    end = padding.end,
    bottom = padding.bottom
)

@Composable
operator fun PaddingValues.times(multiplier: Float) = PaddingValues(
    start = start * multiplier,
    top = top * multiplier,
    end = end * multiplier,
    bottom = bottom * multiplier
)

@Composable
operator fun PaddingValues.plus(dp: Dp) = add(dp)

/**
 * Returns a copy of [PaddingValues] with added padding values.
 */
@Composable
fun PaddingValues.add(dp: Dp) = add(
    start = dp,
    top = dp,
    end = dp,
    bottom = dp
)

/**
 * Returns a copy of [PaddingValues] with added padding values.
 */
@Composable
fun PaddingValues.add(
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp
) = add(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical
)

/**
 * Returns a copy of [PaddingValues] with added padding values.
 */
@Composable
fun PaddingValues.add(
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp
) = PaddingValues(
    start = this.start + start,
    top = this.top + top,
    end = this.end + end,
    bottom = this.bottom + bottom
)

@Composable
fun PaddingValues.exclude(
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) = PaddingValues(
    start = if(start) 0.dp else this.start,
    top = if(top) 0.dp else this.top,
    end = if(end) 0.dp else this.end,
    bottom = if(bottom) 0.dp else this.bottom
)

@Composable
fun PaddingValues.exclude(
    horizontal: Boolean = false,
    vertical: Boolean = false
) = exclude(
    start = horizontal,
    top = vertical,
    end = horizontal,
    bottom = vertical
)

@Composable
fun PaddingValues.only(
	horizontal: Boolean = false,
	vertical: Boolean = false
) = only(
	start = horizontal,
	top = vertical,
	end = horizontal,
	bottom = vertical
)

@Composable
fun PaddingValues.only(
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) = exclude(
    start = !start,
    top = !top,
    end = !end,
    bottom = !bottom
)

@Composable
fun PaddingValues.only(
    horizontal: Boolean? = null,
    vertical: Boolean? = null,
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) = only(
    start = horizontal ?: start,
    top = vertical ?: top,
    end = horizontal ?: end,
    bottom = vertical ?: bottom
)