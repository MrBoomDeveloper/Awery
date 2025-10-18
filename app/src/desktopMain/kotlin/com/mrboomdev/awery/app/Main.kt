package com.mrboomdev.awery.app

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.NavigationState
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.ContextMenu
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.theme.aweryColorScheme
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.currentWindowSize
import de.milchreis.uibooster.UiBooster
import de.milchreis.uibooster.model.UiBoosterOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.DisabledAppearanceValues
import org.jetbrains.jewel.foundation.GlobalColors
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.intui.standalone.theme.IntUiTheme
import org.jetbrains.jewel.intui.standalone.theme.dark
import org.jetbrains.jewel.intui.standalone.theme.darkThemeDefinition
import org.jetbrains.jewel.intui.standalone.theme.default
import org.jetbrains.jewel.intui.window.decoratedWindow
import org.jetbrains.jewel.intui.window.styling.dark
import org.jetbrains.jewel.ui.ComponentStyling
import org.jetbrains.jewel.window.DecoratedWindow
import org.jetbrains.jewel.window.TitleBar
import org.jetbrains.jewel.window.newFullscreenControls
import org.jetbrains.jewel.window.styling.DecoratedWindowColors
import org.jetbrains.jewel.window.styling.DecoratedWindowStyle
import org.jetbrains.jewel.window.styling.TitleBarColors
import org.jetbrains.jewel.window.styling.TitleBarStyle
import java.awt.Dimension
import kotlin.system.exitProcess

@OptIn(ExperimentalFoundationApi::class)
fun main() {
    println("Started Awery Desktop!")
    setupCrashHandler()
    Awery.initEverything()
	
    ComposeFoundationFlags.isNewContextMenuEnabled = false
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
		var currentNavigationState by remember { mutableStateOf(NavigationState(Routes.Home, null)) }
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
								val windowSize = currentWindowSize()

								Crossfade(
									targetState = currentNavigationState.goBack,
									modifier = Modifier
										.fillMaxHeight()
										.aspectRatio(1f)
								) { goBack ->
									if(goBack != null) {
										IconButton(
											padding = 5.dp,
											painter = painterResource(Res.drawable.ic_back),
											contentDescription = null,
											onClick = goBack,
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

								if(windowSize.width >= WindowSizeType.Large) {
									Text(
										fontFamily = AweryFonts.poppins,
										color = colorScheme.onBackground,
										text = "Awery"
									)
								}

								Box(
									modifier = Modifier
										.alpha(animateFloatAsState(if(
											currentNavigationState.route == Routes.Search
										) 1f else 0f).value),
									content = { AwerySearchBar() }
								)
                            }
                        }
                    }
                   
					CompositionLocalProvider(
						LocalTextContextMenu provides AweryContextMenu
					) {
						App(
							onNavigate = {
								currentNavigationState = it
							}
						)
					}

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AwerySearchBar() {
	val windowSize = currentWindowSize()
	val colorScheme = MaterialTheme.colorScheme
	val typography = MaterialTheme.typography

	SwingPanel(
		modifier = Modifier
			.padding(start = when {
				windowSize.width >= WindowSizeType.ExtraLarge -> 128.dp
				windowSize.width >= WindowSizeType.Large -> 64.dp
				else -> 32.dp
			}).padding(vertical = 4.dp)
			.width(when {
				windowSize.width >= WindowSizeType.ExtraLarge -> 500.dp
				windowSize.width >= WindowSizeType.Large -> 400.dp
				windowSize.width >= WindowSizeType.Medium -> 350.dp
				else -> 300.dp
			}).fillMaxHeight(),

		factory = {
			ComposePanel().apply {
				setContent {
					Row(
						modifier = Modifier
							.background(colorScheme.background)
							.clip(RoundedCornerShape(4.dp))
							.background(Color(0x11ffffff))
							.border(.5.dp, Color(0x22ffffff), RoundedCornerShape(4.dp))
							.fillMaxSize()
					) {
						val coroutineScope = rememberCoroutineScope()
						val focusRequester = remember { FocusRequester() }
						val query by App.searchQuery.collectAsState()

						AweryTheme {
							CompositionLocalProvider(
								LocalTextSelectionColors provides TextSelectionColors(
									handleColor = colorScheme.onPrimaryContainer,
									backgroundColor = colorScheme.primaryContainer
								),

								LocalTextContextMenu provides AweryContextMenu
							) {
								BasicTextField(
									modifier = Modifier
										.fillMaxSize()
										.padding(horizontal = 16.dp)
										.focusRequester(focusRequester),
									value = query,
									singleLine = true,
									cursorBrush = SolidColor(colorScheme.onBackground),

									onValueChange = {
										runBlocking {
											App.searchQuery.emit(it)
										}
									},

									textStyle = typography.bodyMedium.copy(
										color = colorScheme.onBackground
									),

									keyboardOptions = KeyboardOptions(
										imeAction = ImeAction.Search
									),

									decorationBox = {
										Box(
											modifier = Modifier
												.fillMaxSize()
												.wrapContentWidth(Alignment.Start),
											contentAlignment = Alignment.Center
										) {
											if(query.isEmpty()) {
												Text(
													style = typography.bodyMedium,
													color = colorScheme.onSurfaceVariant,
													text = "Search"
												)
											}
										}

										Row(
											modifier = Modifier.fillMaxSize(),
											verticalAlignment = Alignment.CenterVertically
										) {
											Box(Modifier.weight(1f)) {
												it()
											}

											if(query.isNotEmpty()) {
												CompositionLocalProvider(
													LocalContentColor provides Color.White
												) {
													IconButton(
														modifier = Modifier
															.fillMaxHeight()
															.aspectRatio(1f)
															.offset(x = 10.dp)
															.pointerHoverIcon(PointerIcon.Hand),

														painter = painterResource(Res.drawable.ic_close),
														contentDescription = null,

														colors = IconButtonDefaults.iconButtonColors(
															contentColor = colorScheme.onSurfaceVariant
														),

														onClick = {
															coroutineScope.launch {
																focusRequester.requestFocus()
																App.searchQuery.emit("")
															}
														}
													)
												}
											}
										}
									}
								)
							}
						}
					}
				}
			}
		}
	)
}

@OptIn(ExperimentalFoundationApi::class)
private object AweryContextMenu: TextContextMenu {
	@Composable
	override fun Area(
		textManager: TextContextMenu.TextManager,
		state: ContextMenuState,
		content: @Composable (() -> Unit)
	) {
		Box(
			modifier = Modifier.contextMenuOpenDetector {
				state.status = ContextMenuState.Status.Open(Rect(it, 0f))
			},

			propagateMinConstraints = true
		) {
			content()

			ContextMenu(
				isVisible = state.status is ContextMenuState.Status.Open,
				onDismissRequest = { state.status = ContextMenuState.Status.Closed }
			) {
				listOf(
					textManager.cut to "Cut",
					textManager.copy to "Copy",
					textManager.paste to "Paste",
					textManager.selectAll to "Select all"
				).filter { it.first != null }.forEach { (onClick, text) ->
					item(
						text = text,
						onClick = onClick!!
					)
				}
			}
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
			t as? Exception ?: kotlin.Exception(t)
        )
    }
}