package com.mrboomdev.awery.ui.screens.intro.steps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.screens.intro.IntroDefaults
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import com.mrboomdev.awery.ui.theme.isDarkTheme
import com.mrboomdev.awery.ui.theme.isMaterialYouAvailable
import com.mrboomdev.awery.ui.theme.materialYouColorScheme
import com.mrboomdev.awery.ui.theme.seedColorScheme
import com.mrboomdev.awery.ui.utils.end
import com.mrboomdev.awery.ui.utils.start
import com.mrboomdev.awery.ui.utils.top
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Serializable
data object IntroThemeStep: IntroStep {
    @Composable
    override fun Content(
        singleStep: Boolean,
        contentPadding: PaddingValues
    ) = IntroDefaults.page(
        contentPadding = contentPadding,
        title = stringResource(Res.string.color_palette),
        description = stringResource(Res.string.color_palette_description),

        icon = {
            Icon(
                modifier = Modifier.size(IntroDefaults.iconSize),
                painter = painterResource(Res.drawable.ic_palette_outlined),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        },

        nextStep = {
            runBlocking { AwerySettings.introDidTheme.set(true) }
            return@page IntroEndStep
        }
    ) { contentPadding ->
        Box(Modifier.padding(contentPadding)) {
            ChildContent(PaddingValues.Zero)
        }
    }

    @Composable
    fun ChildContent(contentPadding: PaddingValues) {
        Column {
            FlowRow(
                modifier = Modifier
                    .padding(
                        start = contentPadding.start,
                        top = contentPadding.top,
                        end = contentPadding.end,
                        bottom = 16.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val materialYou = -0x100000000

                for(primaryColor in listOfNotNull(
                    // All stolen from Dartotsu.
                    // Copying every single one property would be pain in as to maintain,
                    // so instead we do just generate a whole palette based of a primary color.
                    materialYou.takeIf { isMaterialYouAvailable() },
                    0xFF00658E, // Blue
                    0xFF426916, // Green
                    0xFF6750A4, // Lavender
                    0xFF008F8C, // Ocean
                    0xFFFF9800, // Oriax
                    0xFFFF92E2, // Pink
                    0xFF7C4997, // Purple
                    0xFFC0000A, // Red
                    0xFFFF007F // Saikou
                )) {
                    val scheme = if(primaryColor == materialYou) {
                        materialYouColorScheme()
                    } else seedColorScheme(Color(primaryColor))

                    Card(
                        modifier = Modifier.size(48.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary
                        ),

                        onClick = {
                            runBlocking {
                                AwerySettings.primaryColor.set(primaryColor)
                            }
                        }
                    ) {
                        if(AwerySettings.primaryColor.collectAsState().value.let {
                            it == primaryColor || (primaryColor == materialYou && it < 0L)
                        }) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                painter = painterResource(Res.drawable.ic_done),
                                contentDescription = null
                            )
                        } else if(primaryColor == materialYou) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                painter = painterResource(Res.drawable.ic_awesome_filled),
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            val buttonPadding = PaddingValues(
                start = contentPadding.start,
                top = 12.dp,
                end = contentPadding.end,
                bottom = 12.dp
            )

            if(!Awery.isTv) {
                SettingsDefaults.itemSetting(
                    setting = AwerySettings.darkTheme,
                    contentPadding = buttonPadding,
                    icon = painterResource(Res.drawable.ic_dark_mode_outlined),
                    title = stringResource(Res.string.dark_theme),
                    description = stringResource(Res.string.dark_theme_description),
                    enumValues = {
                        when(it) {
                            AwerySettings.DarkTheme.AUTO -> "Auto"
                            AwerySettings.DarkTheme.ON -> "On"
                            AwerySettings.DarkTheme.OFF -> "Off"
                        }
                    }
                )
            }

            AnimatedVisibility(isDarkTheme() && !Awery.isTv) {
                SettingsDefaults.itemSetting(
                    setting = AwerySettings.amoledTheme,
                    contentPadding = buttonPadding,
                    icon = painterResource(Res.drawable.ic_contrast),
                    title = stringResource(Res.string.amoled),
                    description = stringResource(Res.string.amoled_theme_description)
                )
            }
        }
    }
}