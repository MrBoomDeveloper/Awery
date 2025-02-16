package com.mrboomdev.awery.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ext.data.CatalogVideoFile
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n
import com.mrboomdev.awery.ui.components.VideoPlayer
import com.mrboomdev.awery.ui.components.rememberVideoPlayerState
import com.mrboomdev.awery.ui.utils.ControlInsets
import com.mrboomdev.awery.ui.utils.InsetsVisibility
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.reflect.KClass

@Composable
fun PlayerScreen(
	media: CatalogMedia?,
	initialEpisode: Int,
	episodes: List<CatalogVideoFile>,
	viewModel: PlayerViewModel = viewModel(factory = object : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
			@Suppress("UNCHECKED_CAST")
			return PlayerViewModel(
				initialEpisode
			) as T
		}
	})
) {
	val navigator = LocalNavigator.currentOrThrow
	val currentEpisode = episodes[viewModel.currentEpisode]
	
	ControlInsets(InsetsVisibility.HIDDEN)
	
	Box(Modifier.background(Color.Black)) { 
		val playerState = rememberVideoPlayerState()
		
		VideoPlayer(
			modifier = Modifier.fillMaxSize(),
			url = currentEpisode.url,
			state = playerState
		)
		
		Column {
			Row {
				IconButton(
					onClick = { navigator.pop() }
				) {
					Icon(
						painter = painterResource(Res.drawable.ic_language),
						contentDescription = stringResource(Res.string.back),
						tint = Color.White
					)
				}
				
				Text(
					text = currentEpisode.title ?: media?.title?.let { 
						"$it ${i18n(Res.string.episode)} $currentEpisode" 
					} ?: currentEpisode.url,
					
					color = Color.White,
					style = MaterialTheme.typography.titleLarge
				)
			}
			
			Row {
				// Pause
			}
			
			Row {
				//Seekbar
			}
		}
	}
}