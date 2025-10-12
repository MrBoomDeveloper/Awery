package com.mrboomdev.awery.app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.LocalTextContextMenu
import androidx.compose.foundation.text.TextContextMenu
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.*
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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.utils.collection.iterate
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.App
import com.mrboomdev.awery.ui.Routes
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.getInitialRoute
import com.mrboomdev.awery.ui.theme.AweryTheme
import com.mrboomdev.awery.ui.theme.aweryColorScheme
import com.mrboomdev.awery.ui.utils.KeyboardAction
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.navigation.core.safePop
import com.mrboomdev.navigation.jetpack.rememberJetpackNavigation
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
        val navigation = rememberJetpackNavigation<Routes>(getInitialRoute())
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
                                navigation.currentBackStackFlow.collectAsState(null).value 
								val windowSize = currentWindowSize()
								val typography = MaterialTheme.typography

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

								if(windowSize.width >= WindowSizeType.Large) {
									Text(
										fontFamily = AweryFonts.poppins,
										color = colorScheme.onBackground,
										text = "Awery"
									)
								}

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

																keyboardActions = KeyboardAction {
																	navigation.clear()
																	navigation.push(Routes.Search)
																},

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
                        }
                    }
                   
					CompositionLocalProvider(
						LocalTextContextMenu provides AweryContextMenu
					) {
						App()
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

			if(state.status is ContextMenuState.Status.Open) {
				Popup(
					onDismissRequest = {
						state.status = ContextMenuState.Status.Closed
					}
				) {
					val shape = RoundedCornerShape(8.dp)

					Column(
						modifier = Modifier
							.clip(shape)
							.background(MaterialTheme.colorScheme.surfaceContainerHighest)
							.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .05f))
							.border(.5.dp, MaterialTheme.colorScheme.surfaceContainerLowest, shape)
							.width(175.dp)
							.verticalScroll(rememberScrollState())
							.padding(vertical = 2.dp)
					) {
						data class Action(
							val onClick: (() -> Unit)?,
							val text: String,
							val hideMenu: Boolean = true
						)

						listOf(
							Action(textManager.cut, "Cut"),
							Action(textManager.copy, "Copy"),
							Action(textManager.paste, "Paste"),
							Action(textManager.selectAll, "Select all", hideMenu = false)
						).iterate { (onClick, text, hideMenu) ->
							if(onClick == null) return@iterate

							CompositionLocalProvider(
								LocalContentColor provides MaterialTheme.colorScheme.primary
							) {
								Text(
									modifier = Modifier
										.clip(RoundedCornerShape(4.dp))
										.clickable(onClick = {
											onClick()

											if(hideMenu) {
												state.status = ContextMenuState.Status.Closed
											}
										}).padding(horizontal = 16.dp, vertical = 10.dp)
										.fillMaxWidth(),
									style = MaterialTheme.typography.bodySmall,
									color = MaterialTheme.colorScheme.onSurface,
									text = text
								)
							}

							if(hasNext()) {
								HorizontalDivider(
									modifier = Modifier.alpha(.5f),
									color = MaterialTheme.colorScheme.secondaryContainer
								)
							}
						}
					}
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