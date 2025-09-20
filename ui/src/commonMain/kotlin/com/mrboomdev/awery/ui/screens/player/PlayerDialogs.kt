package com.mrboomdev.awery.ui.screens.player

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.components.AlertDialog

internal interface PlayerDialog {
    @Composable
    operator fun invoke()
}

internal class SubtitlesDialog(
    private val onDismissRequest: (PlayerDialog) -> Unit
): PlayerDialog {
    @Composable
    override fun invoke() {
        AlertDialog(
            onDismissRequest = { onDismissRequest(this) }
        ) {
            Text("Subtitles dialog isn't finished yet!")
        }
    }
}

internal class QualityDialog(
    private val onDismissRequest: (PlayerDialog) -> Unit
): PlayerDialog {
    @Composable
    override fun invoke() {
        AlertDialog(
            onDismissRequest = { onDismissRequest(this) }
        ) {
            Text("Quality dialog isn't finished yet!")
        }
    }
}