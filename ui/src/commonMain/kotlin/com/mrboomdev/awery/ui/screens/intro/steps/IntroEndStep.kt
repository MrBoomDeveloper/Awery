package com.mrboomdev.awery.ui.screens.intro.steps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.navigation.MainRoutes
import com.mrboomdev.awery.ui.navigation.Navigation
import com.mrboomdev.awery.ui.screens.intro.IntroDefaults
import com.mrboomdev.navigation.core.safePop
import io.github.vinceglb.confettikit.compose.ConfettiKit
import io.github.vinceglb.confettikit.core.Party
import io.github.vinceglb.confettikit.core.Position
import io.github.vinceglb.confettikit.core.emitter.Emitter
import io.github.vinceglb.confettikit.core.models.Shape
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.milliseconds

@Serializable
data object IntroEndStep: IntroStep {
    private fun createParty(
        durationMs: Int,
        amountPerSec: Int,
        delayMs: Int,
        spread: Int,
        angle: Int,
        x: Double
    ) = Party(
        emitter = Emitter(durationMs.milliseconds).perSecond(amountPerSec),
        shapes = listOf(Shape.Square, Shape.Circle),
        position = Position.Relative(x, .3),
        delay = delayMs,
        spread = spread,
        angle = angle,
        timeToLive = 4000,
        speed = 0f,
        maxSpeed = 30f
    )

    @Composable
    override fun Content(
        singleStep: Boolean,
        contentPadding: PaddingValues
    ) {
        ConfettiKit(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                createParty(250, 300, 0, 360, 0, .5),
                createParty(275, 225, 300, 200, 150, .4),
                createParty(275, 225, 300, 200, 330, .6)
            )
        )

        IntroDefaults.page(
            contentPadding = contentPadding,
            title = stringResource(Res.string.status_finished),
            description = stringResource(Res.string.setup_finished_description),

            icon = {
                Icon(
                    modifier = Modifier.size(IntroDefaults.iconSize),
                    painter = painterResource(Res.drawable.ic_done),
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )
            },

            actions = {
                val introNavigation = IntroDefaults.navigation.current()
                val appNavigation = Navigation.current()

                TextButton(onClick = { introNavigation.safePop() }) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(Res.string.back)
                    )
                }

                Button(onClick = {
                    appNavigation.clear()
                    appNavigation.push(MainRoutes.entries[AwerySettings.mainDefaultTab.value.ordinal].route)
                }) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(Res.string.finish)
                    )
                }
            }
        )
    }
}