package com.mrboomdev.awery.ui.screens.player

import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.activity
import com.mrboomdev.awery.ui.components.MediaPlayerState

internal actual fun initPlayer(player: MediaPlayerState) {
    player.init(Awery.activity!!)
}