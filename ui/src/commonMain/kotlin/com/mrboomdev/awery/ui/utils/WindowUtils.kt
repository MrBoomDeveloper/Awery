package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val noneImpl = WindowInsets(0, 0, 0, 0)
val WindowInsets.Companion.none get() = noneImpl

val WindowInsets.top
    @Composable
    get() = with(LocalDensity.current) {
        getTop(this).toDp()
    }

val WindowInsets.bottom
    @Composable
    get() = with(LocalDensity.current) {
        getBottom(this).toDp()
    }

val WindowInsets.left
    @Composable
    get() = with(LocalDensity.current) {
        getLeft(this, LocalLayoutDirection.current).toDp()
    }

val WindowInsets.right
    @Composable
    get() = with(LocalDensity.current) {
        getRight(this, LocalLayoutDirection.current).toDp()
    }

enum class WindowSizeType(
    val widthAtLeast: Dp,
    val heightAtLeast: Dp
) {
    Small(
        widthAtLeast = 0.dp,
        heightAtLeast = 0.dp
    ),
    
    Medium(
        widthAtLeast = 600.dp,
        heightAtLeast = 480.dp
    ),
    
    Large(
        widthAtLeast = 840.dp,
        heightAtLeast = 900.dp
    ),
    
    ExtraLarge(
        widthAtLeast = 1200.dp,
        heightAtLeast = 900.dp
    )
}

data class WindowSize(
    val width: WindowSizeType,
    val height: WindowSizeType
)

@Composable
fun currentWindowHeight() = with(LocalDensity.current) {
    LocalWindowInfo.current.containerSize.height.toDp()
}

@Composable
fun currentWindowWidth() = with(LocalDensity.current) {
    LocalWindowInfo.current.containerSize.width.toDp()
}

@Composable
fun currentWindowSize(): WindowSize {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    
    return remember(size, density) { 
        with(density) {
            WindowSize(
                width = WindowSizeType.entries.last {
                    size.width.toDp() >= it.widthAtLeast
                },

                height = WindowSizeType.entries.last {
                    size.height.toDp() >= it.heightAtLeast
                }
            )
        }
    }
}

@Stable
fun WindowInsets(horizontal: Dp = 0.dp, vertical: Dp = 0.dp) = WindowInsets(
    left = horizontal,
    top= vertical,
    right = horizontal,
    bottom = vertical
)

@Composable
fun niceSideInset(): Dp {
	val windowSize = currentWindowSize()
	
	return when {
		windowSize.width >= WindowSizeType.ExtraLarge -> 64.dp
		windowSize.width >= WindowSizeType.Large -> 32.dp
		else -> 16.dp
	}
}