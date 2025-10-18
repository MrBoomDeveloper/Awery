package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun Modifier.combinedClickable(onLongClick: () -> Unit): Modifier {
	return combinedClickable(onClick = {}, onLongClick = onLongClick)
}

fun Modifier.scaleX(x: Float) =
    scale(scaleX = x, scaleY = 1f)

fun Modifier.scaleY(y: Float) =
    scale(scaleX = 1f, scaleY = y)

@OptIn(ExperimentalContracts::class)
inline fun Modifier.thenScope(scope: Modifier.() -> Modifier): Modifier {
    contract {
        callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
    }

    val result = scope()
    return if(result == Modifier) this else result
}

@OptIn(ExperimentalContracts::class)
inline fun Modifier.thenIf(
    run: Boolean,
    scope: Modifier.() -> Modifier
): Modifier {
    contract {
        callsInPlace(scope, InvocationKind.AT_MOST_ONCE)
    }

    val result = if(run) scope() else this
    return if(result == Modifier) this else result
}

@OptIn(ExperimentalContracts::class)
inline fun Modifier.thenIfElse(
    run: Boolean,
    scopeIf: Modifier.() -> Modifier,
    scopeElse: Modifier.() -> Modifier
): Modifier {
    contract {
        callsInPlace(scopeIf, InvocationKind.AT_MOST_ONCE)
        callsInPlace(scopeElse, InvocationKind.AT_MOST_ONCE)
    }

    val result = if(run) scopeIf() else scopeElse()
    return if(result == Modifier) this else result
}

fun Modifier.padding(
	horizontal: Dp? = null,
	vertical: Dp? = null,
	start: Dp = 0.dp,
	end: Dp = 0.dp,
	top: Dp = 0.dp,
	bottom: Dp = 0.dp
) = padding(
	start = horizontal ?: start, 
	end = horizontal ?: end, 
	top = vertical ?: top, 
	bottom = vertical ?: bottom
)

/**
 * Detects events that open a context menu (mouse right-clicks).
 *
 * @param key The pointer input handling coroutine will be cancelled and **re-started** when
 * [contextMenuOpenDetector] is recomposed with a different [key].
 * @param enabled Whether to enable the detection.
 * @param onOpen Invoked when a context menu opening event is detected, with the local offset it
 * should be opened at.
 */
fun Modifier.contextMenuOpenDetector(
	key: Any? = Unit,
	enabled: Boolean = true,
	onOpen: (Offset) -> Unit
): Modifier {
	return if (enabled) {
		this.pointerInput(key) {
			awaitEachGesture {
				val event = awaitEventFirstDown()
				if (event.buttons.isSecondaryPressed) {
					event.changes.forEach { it.consume() }
					onOpen(event.changes[0].position)
				}
			}
		}
	} else {
		this
	}
}

private suspend fun AwaitPointerEventScope.awaitEventFirstDown(): PointerEvent {
	var event: PointerEvent
	
	do {
		event = awaitPointerEvent()
	} while (
		!event.changes.fastAll { it.changedToDown() }
	)
	
	return event
}