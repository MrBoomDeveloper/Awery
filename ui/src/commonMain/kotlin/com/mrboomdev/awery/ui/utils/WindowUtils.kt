package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import javax.swing.Spring.height

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
    internal val widthAtLeast: Dp,
    internal val heightAtLeast: Dp
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