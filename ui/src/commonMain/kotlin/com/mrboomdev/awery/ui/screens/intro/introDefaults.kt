package com.mrboomdev.awery.ui.screens.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.back
import com.mrboomdev.awery.resources.continue_action
import com.mrboomdev.awery.resources.done
import com.mrboomdev.awery.ui.Navigation
import com.mrboomdev.awery.ui.utils.WindowSizeType
import com.mrboomdev.awery.ui.utils.add
import com.mrboomdev.awery.ui.utils.currentWindowSize
import com.mrboomdev.navigation.core.TypeSafeNavigation
import com.mrboomdev.navigation.core.safePop
import org.jetbrains.compose.resources.stringResource

internal object IntroDefaults {
	val navigation = TypeSafeNavigation<IntroStep>()
	val iconSize = 48.dp
	private val spaceBetweenActions = 16.dp
	private val contentPadding = 32.dp
	
	@Composable
	fun page(
		icon: @Composable () -> Unit,
		title: String,
		description: String,
		actions: @Composable RowScope.() -> Unit,
		content: @Composable (PaddingValues) -> Unit = {}
	) {
		val windowSize = currentWindowSize()
		
		if(windowSize.width >= WindowSizeType.Large) {
			Row(
				modifier = Modifier.fillMaxSize(),
				horizontalArrangement = Arrangement.spacedBy(32.dp)
			) { 
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.verticalScroll(rememberScrollState())
						.windowInsetsPadding(WindowInsets.safeContent.only(
							WindowInsetsSides.Vertical + WindowInsetsSides.Start
						)).padding(contentPadding),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					icon()
					
					Text(
						modifier = Modifier.padding(top = 8.dp),
						style = MaterialTheme.typography.headlineLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Normal,
						text = title
					)
					
					Text(description)
				}
				
				Box(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
				) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(rememberScrollState())
					) {
						content(WindowInsets.safeContent.only(
							WindowInsetsSides.Top + WindowInsetsSides.End
						).asPaddingValues().add(contentPadding).add(bottom = 64.dp))
					}

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.windowInsetsPadding(WindowInsets.safeContent.only(
								WindowInsetsSides.Bottom + WindowInsetsSides.End
							)).padding(
								start = contentPadding,
								top = contentPadding / 2,
								end = contentPadding, 
								bottom = contentPadding
							)
							.align(Alignment.BottomCenter),
						horizontalArrangement = Arrangement.spacedBy(spaceBetweenActions, Alignment.End)
					) {
						actions()
					}
				}
			}
		} else {
			Column(Modifier.fillMaxSize()) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f)
						.verticalScroll(rememberScrollState())
						.windowInsetsPadding(WindowInsets.safeContent.only(
							WindowInsetsSides.Top + WindowInsetsSides.Horizontal
						)).padding(contentPadding),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					icon()

					Text(
						modifier = Modifier.padding(top = 8.dp),
						style = MaterialTheme.typography.headlineLarge,
						color = MaterialTheme.colorScheme.onBackground,
						fontWeight = FontWeight.Normal,
						text = title
					)

					Text(description)

					Box {
						content(WindowInsets.safeContent.only(
							WindowInsetsSides.Horizontal
						).asPaddingValues().add(vertical = 16.dp))
					}
				}
				
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.windowInsetsPadding(WindowInsets.safeContent.only(
							WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
						)).padding(start = contentPadding, end = contentPadding, bottom = contentPadding),
					horizontalArrangement = Arrangement.SpaceBetween
				) { 
					actions()
				}
			}
		}
	}

	@Composable
	fun page(
		icon: @Composable () -> Unit,
		title: String,
		description: String,
		canOpenNextStep: Boolean = true,
		nextStep: (() -> IntroStep)?,
		content: @Composable (PaddingValues) -> Unit = {}
	) = page(
		icon = icon,
		title = title,
		description = description,
		content = content,
		
		actions = {
			val introNavigation = navigation.current()
			val appNavigation = Navigation.current()
			
			if(nextStep == null) {
				Spacer(Modifier.weight(1f))
				
				Button(
					enabled = canOpenNextStep,
					onClick = { appNavigation.safePop() }
				) {
					Text(stringResource(Res.string.done))
				}
			} else {
				TextButton(onClick = { introNavigation.safePop() }) {
					Text(
						modifier = Modifier.padding(horizontal = 16.dp),
						text = stringResource(Res.string.back)
					)
				}

				Button(
					enabled = canOpenNextStep,
					onClick = { introNavigation.push(nextStep()) }
				) {
					Text(
						modifier = Modifier.padding(horizontal = 16.dp),
						text = stringResource(Res.string.continue_action)
					)
				}
			}
		}
	)
}