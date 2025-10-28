package com.mrboomdev.awery.ui.screens.intro

import androidx.compose.foundation.layout.*
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
import com.mrboomdev.awery.ui.screens.intro.steps.IntroStep
import com.mrboomdev.awery.ui.utils.*
import com.mrboomdev.navigation.core.TypeSafeNavigation
import com.mrboomdev.navigation.core.safePop
import org.jetbrains.compose.resources.stringResource

internal object IntroDefaults {
	val navigation = TypeSafeNavigation<IntroStep>()
	val iconSize = 48.dp
	val spaceBetweenActions = 16.dp
//	private val contentPaddingDp = 32.dp
	
	@Composable
	fun page(
		contentPadding: PaddingValues,
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
				horizontalArrangement = Arrangement.spacedBy(64.dp)
			) { 
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f)
						.verticalScroll(rememberScrollState())
						.padding(contentPadding.only(top = true, horizontal = true))
						.padding(start = niceSideInset()),
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
						content(contentPadding.only(
							top = true, end = true
						).add(bottom = 64.dp, end = niceSideInset()))
					}

					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(contentPadding.only(bottom = true, end = true))
							.padding(end = niceSideInset())
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
						.padding(contentPadding.only(top = true, start = true, end = true)),
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
						content(contentPadding.only(
							/*horizontal = true*/
						).add(vertical = 16.dp))
					}
				}
				
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(contentPadding.only(horizontal = true, bottom = true)),
					horizontalArrangement = Arrangement.SpaceBetween
				) { 
					actions()
				}
			}
		}
	}

	@Composable
	fun page(
		contentPadding: PaddingValues,
		icon: @Composable () -> Unit,
		title: String,
		description: String,
		canOpenNextStep: Boolean = true,
		nextStep: (() -> IntroStep)?,
		content: @Composable (PaddingValues) -> Unit = {}
	) = page(
		contentPadding = contentPadding,
		icon = icon,
		title = title,
		description = description,
		content = content,
		
		actions = {
			if(nextStep == null) {
				Spacer(Modifier.weight(1f))
				return@page
			}
			
			val introNavigation = navigation.current()
				
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
	)
}