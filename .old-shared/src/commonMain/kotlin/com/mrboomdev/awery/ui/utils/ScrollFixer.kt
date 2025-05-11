package com.mrboomdev.awery.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import kotlinx.coroutines.delay

/**
 * If you'll try to add a second item into the list without an delay, 
 * list will automatically scroll to bottom which is an bad behaviour.
 */
private const val DELAY_AFTER_FIRST_ITEM_ADDITION = 500L

class ScrollFixer {
	private var didFix = false
	
	suspend fun fix(state: LazyListState) = fixImpl {
		state.scrollToItem(0)
		delay(DELAY_AFTER_FIRST_ITEM_ADDITION)
		state.scrollToItem(0)
	}
	
	suspend fun fix(state: LazyGridState) = fixImpl {
		state.scrollToItem(0)
		delay(DELAY_AFTER_FIRST_ITEM_ADDITION)
		state.scrollToItem(0)
	}
	
	private suspend fun fixImpl(solution: suspend () -> Unit) {
		if(didFix) return
		solution()
		didFix = true
	}
}