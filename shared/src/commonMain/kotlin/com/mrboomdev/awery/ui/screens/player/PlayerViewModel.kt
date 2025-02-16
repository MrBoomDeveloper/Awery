package com.mrboomdev.awery.ui.screens.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class PlayerViewModel(
	initialEpisode: Int
): ViewModel() {
	var currentEpisode by mutableIntStateOf(initialEpisode)
}