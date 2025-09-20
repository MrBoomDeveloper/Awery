package com.mrboomdev.awery.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.compose.AsyncImagePainter.State.Empty.painter
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.resources.AweryFonts
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.theme.aweryColorScheme
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
import de.milchreis.uibooster.UiBooster
import de.milchreis.uibooster.model.UiBoosterOptions
import kotlinx.serialization.json.JsonNull.content
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.BorderColors
import org.jetbrains.jewel.foundation.DisabledAppearanceValues
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.OutlineColors
import org.jetbrains.jewel.foundation.TextColors
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.foundation.theme.ThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.createDefaultTextStyle
import org.jetbrains.jewel.intui.standalone.theme.createEditorTextStyle
import org.jetbrains.jewel.intui.standalone.theme.dark
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.defaultDecoratedWindowStyle
import org.jetbrains.jewel.window.defaultTitleBarStyle
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.DecoratedWindowColors
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.LocalDecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.awt.Dimension
import java.awt.SystemColor.text

fun main() {
    println("Started Awery Desktop!")
    setupCrashHandler()
    Awery.initEverything()

    application {
        val navigation = rememberJetpackNavigation<Routes>(Routes.Main)
        val colorScheme = aweryColorScheme()
        
        IntUiTheme(
            theme = JewelTheme.darkThemeDefinition(
                colors = GlobalColors.dark(
                    paneBackground = colorScheme.background
                ),
                
                disabledAppearanceValues = DisabledAppearanceValues.dark()
            ),
            
            styling = ComponentStyling.default().decoratedWindow(
                windowStyle = DecoratedWindowStyle.dark(
                    colors = DecoratedWindowColors.dark(
                        borderColor = colorScheme.outlineVariant
                    )
                )
            ),
            
            swingCompatMode = false
        ) {
            DecoratedWindow(
                icon = painterResource(Res.drawable.logo_awery),
                title = "Awery",
                onCloseRequest = ::exitApplication,
                
                content = {
                    TitleBar(
                        modifier = Modifier.newFullscreenControls(),
                        style = TitleBarStyle.dark(
                            colors = TitleBarColors.dark(
                                backgroundColor = colorScheme.background,
                                inactiveBackground = colorScheme.background,
                                contentColor = colorScheme.onBackground,
                                borderColor = colorScheme.outlineVariant
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Start)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentRoute by navigation.currentDestination.collectAsState(null)
                            
                            Crossfade(
                                targetState = navigation.canPop,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                            ) { canPop ->
                                if(canPop) {
                                    IconButton(
                                        painter = painterResource(Res.drawable.ic_back),
                                        contentDescription = null,
                                        onClick = { navigation.safePop() },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            contentColor = colorScheme.onBackground
                                        )
                                    )
                                    
                                    return@Crossfade
                                }
                                
                                Image(
                                    modifier = Modifier.padding(10.dp),
                                    painter = painterResource(Res.drawable.logo_awery),
                                    contentDescription = null
                                )
                            }

                            Text(
                                fontFamily = AweryFonts.poppins,
                                color = colorScheme.onBackground,
                                text = when(val route = currentRoute) {
                                    is Routes.Media -> route.media.title
                                    is Routes.Settings -> "Settings"
									is Routes.Browser -> route.url
									is Routes.Extension -> route.extensionName
									is Routes.ExtensionFeed -> "${route.extensionName} - ${route.feedName}"
									is Routes.ExtensionSearch -> "${route.extensionName} - Search"
									is Routes.Intro -> "Intro"
									is Routes.Player -> "Player"
									Routes.Main, null -> "Awery"
								}
                            )
                        }
                    }
                    
                    App(
                        navigation = { navigation }
                    )

                    LaunchedEffect(window) {
                        window.minimumSize = Dimension(
                            600, 380
                        )
                    }
                }
            )
        }
    }
}

private fun setupCrashHandler() {
    Thread.setDefaultUncaughtExceptionHandler { _, t ->
        t.printStackTrace()

        UiBooster(
            UiBoosterOptions(FlatOneDarkIJTheme(), null, null)
        ).showException(
            "Please report this problem on GitHub issues at https://github.com/MrBoomDeveloper/Awery",
            "Awery has crashed!",
			t as? Exception ?: Exception(t)
        )
    }
}