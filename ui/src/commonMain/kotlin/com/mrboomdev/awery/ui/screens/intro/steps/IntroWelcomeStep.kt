package com.mrboomdev.awery.ui.screens.intro.steps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.lets_begin
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.resources.welcome_to_app_description
import com.mrboomdev.awery.ui.screens.intro.IntroDefaults
import com.mrboomdev.awery.ui.screens.intro.IntroDslWrapper
import com.mrboomdev.awery.ui.screens.intro.setAlignment
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Serializable
data object IntroWelcomeStep: IntroStep {
    @Composable
    override fun Content(
        singleStep: Boolean,
        contentPadding: PaddingValues
    ) {
        val navigation = IntroDefaults.navigation.current()
        val continueLabel = stringResource(Res.string.lets_begin)

        IntroDslWrapper(contentPadding) {
            setAlignment(Alignment.CenterHorizontally)
            actionScale = 1.1f
            iconSize = 128.dp
            icon = { painterResource(Res.drawable.logo_awery) }
            title = { "Awery" }
            description = { stringResource(Res.string.welcome_to_app_description) }

            addAction {
                text = continueLabel
                onClick = {
                    runBlocking { AwerySettings.introDidWelcome.set(true) }
                    navigation.push(IntroAccountStep)
                }
            }
        }
    }
}