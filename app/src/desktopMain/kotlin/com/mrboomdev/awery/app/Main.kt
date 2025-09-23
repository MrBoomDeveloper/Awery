package com.mrboomdev.awery.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImagePainter.State.Empty.painter
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.resources.AweryFonts
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.resources.logo_awery
import com.mrboomdev.awery.resources.search
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.theme.aweryColorScheme
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
import de.milchreis.uibooster.UiBooster
import de.milchreis.uibooster.model.UiBoosterOptions
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.resolve
import io.github.vinceglb.filekit.writeString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull.content
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
import org.jetbrains.jewel.ui.component.ContextMenuDivider.onClick
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
import kotlin.system.exitProcess

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    println("Started Awery Desktop!")
    setupCrashHandler()
    Awery.initEverything()
    
    ComposeFoundationFlags.isNewContextMenuEnabled = true
    ComposeFoundationFlags.isTextFieldDpadNavigationEnabled = true
    ComposeFoundationFlags.isSmartSelectionEnabled = true
    
//    val windowState = FileKit.filesDir.resolve("window.json").takeIf { it.exists() }?.let {
//        try {
//            Json.decodeFromString<SavedWindowState>(it.readString()).restoreState()
//        } catch(e: Exception) {
//            Log.e("Main", "Failed to restore window state!", e)
//            null
//        }
//    } ?: WindowState()

    application(exitProcessOnExit = false) {
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
            )
        ) {
            DecoratedWindow(
                icon = painterResource(Res.drawable.logo_awery),
                title = "Awery",
                onCloseRequest = ::exitApplication,
//                state = windowState,
                
                state = rememberWindowState(
                    size = DpSize(
                        width = 900.dp,
                        height = 600.dp
                    )
                ),
                
                content = {
                    TitleBar(
                        modifier = Modifier.newFullscreenControls(),
                        style = TitleBarStyle.dark(
                            colors = TitleBarColors.dark(
                                backgroundColor = colorScheme.background,
                                inactiveBackground = colorScheme.background,
                                contentColor = colorScheme.onBackground,
                                borderColor = colorScheme.background
                            )
                        )
                    ) {
                        AweryTheme {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                navigation.currentBackStack.collectAsState(null).value
                                
                                Crossfade(
                                    targetState = navigation.canPop,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                ) { canPop ->
                                    if(canPop) {
                                        IconButton(
                                            padding = 5.dp,
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
                                    text = "Awery"
                                )
                                
//                                Row(
//                                    modifier = Modifier
//                                        .padding(vertical = 6.dp, horizontal = 64.dp)
//                                        .width(400.dp)
//                                        .fillMaxHeight()
//                                        .clip(RoundedCornerShape(4.dp))
//                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
//                                        .padding(horizontal = 16.dp)
//                                ) {
//                                    var text by remember { mutableStateOf("") }
//
//                                    Box(
//                                        modifier = Modifier
//                                            .fillMaxHeight()
//                                            .weight(1f),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        val focusRequester = remember { FocusRequester() }
//                                        
//                                        BasicTextField(
//                                            modifier = Modifier
//                                                .focusRequester(focusRequester)
//                                                .fillMaxWidth(),
//                                            value = text,
//                                            onValueChange = { text = it },
//                                            textStyle = MaterialTheme.typography.bodySmall.copy(
//                                                color = colorScheme.onSurface
//                                            ),
//                                            singleLine = true,
//                                            decorationBox = { content ->
//                                                if(text.isEmpty()) {
//                                                    Text(
//                                                        style = MaterialTheme.typography.bodySmall,
//                                                        color = MaterialTheme.colorScheme.secondary,
//                                                        text = "Search anything"
//                                                    )
//                                                }
//                                                
//                                                content()
//                                            }
//                                        )
//                                    }
//                                }
                            }
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

//    FileKit.filesDir.resolve("window.json").apply {
//        writeString(Json.encodeToString(windowState.saveState()))
//    }
//    
//    Log.i("Main", "Awery closed. Window state saved successfully!")
    exitProcess(0)
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