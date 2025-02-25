package com.mrboomdev.awery.ui.routes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.saveable
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mrboomdev.awery.ext.data.Setting
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.sources.ExtensionsManager
import com.mrboomdev.awery.ui.utils.LocalToaster
import com.mrboomdev.awery.ui.utils.screenModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class SearchRoute(
	initialFilters: List<@Contextual Setting>? = null
): DefaultSearchRoute(initialFilters)

@Serializable
open class DefaultSearchRoute(
	private val initialFilters: List<@Contextual Setting>? = null
): BaseRoute() {
	@OptIn(ExperimentalFoundationApi::class)
	@Composable
	override fun Content() {
		val navigation = LocalNavigator.currentOrThrow
		val screenModel = screenModel { SearchModel(it) }
		val queryFocusRequested = remember { FocusRequester() }
		
		val foundSources by remember { derivedStateOf {
			ExtensionsManager.allSources.filter { source ->
				screenModel.query.text.trim().let { text ->
					source.context.name?.contains(text) == true || source.context.id.contains(text)
				}
			}.sortedBy { it.context.name ?: it.context.id }
		} }
		
		LaunchedEffect(true) {
			queryFocusRequested.requestFocus()
		}
		
		Column {
			Row(
				modifier = Modifier.windowInsetsPadding(
					WindowInsets.safeDrawing.only(
						WindowInsetsSides.Horizontal + WindowInsetsSides.Top
					)
				),
				verticalAlignment = Alignment.CenterVertically
			) {
				IconButton(
					onClick = { navigation.pop() }
				) {
					Icon(
						modifier = Modifier
							.size(64.dp)
							.padding(9.dp),
						painter = painterResource(Res.drawable.ic_back),
						contentDescription = stringResource(Res.string.back)
					)
				}
				
				BasicTextField(
					modifier = Modifier
						.focusRequester(queryFocusRequested)
						.padding(4.dp)
						.fillMaxWidth(),
					
					state = screenModel.query,
					lineLimits = TextFieldLineLimits.SingleLine,
					cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
					
					textStyle = TextStyle(
						color = MaterialTheme.colorScheme.onSurface,
						fontSize = 16.sp
					)
				)
			}
			
			LazyColumn(
				modifier = Modifier.fillMaxWidth(),
				contentPadding = WindowInsets.safeDrawing.only(
					WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
				).asPaddingValues()
			) {
				items(
					items = foundSources,
					key = { it.context.manager.context.id + it.context.id }
				) { source ->
					val toaster = LocalToaster.current
					
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.clickable {
								toaster.show("This action isn't done yet!")
							}.padding(8.dp)
							.animateItemPlacement()
					) {
						Text(
							style = MaterialTheme.typography.bodyLarge,
							text = source.context.name ?: source.context.id
						)
					}
				}
				
				if(foundSources.isEmpty()) {
					item("empty") {
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.padding(32.dp, 64.dp)
								.animateItemPlacement(),
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							Text(
								style = MaterialTheme.typography.headlineSmall,
								text = stringResource(Res.string.nothing_found)
							)
							
							Text(
								modifier = Modifier.padding(8.dp),
								textAlign = TextAlign.Center,
								text = stringResource(Res.string.no_media_found)
							)
						}
					}
				}
			} 
		}
	}
}

private class SearchModel(savedState: SavedStateHandle): ScreenModel {
	val query by savedState.saveable(saver = TextFieldState.Saver) { TextFieldState() }
}