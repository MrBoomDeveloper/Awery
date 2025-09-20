package com.mrboomdev.awery.extension.loaders.watch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.loaders.Extensions
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.extensions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

class ExtensionsWatcher(
	private val ogExtension: String,
	val media: Media,
	private val coroutineScope: CoroutineScope
): WatcherNode.Variants {
	private val _children = mutableStateListOf<WatcherNode>()
	override val children: List<WatcherNode> = _children
	
	private val _isLoading = mutableStateOf(true)
	override val isLoading by _isLoading

	override val id = "extensions"
	
	override val title by lazy { 
		runBlocking { 
			getString(Res.string.extensions)
		}
	}

	override suspend fun load() {
		_isLoading.value = true
		
		Extensions.getAll<WatchModule>(enabled = true).filter {
			when(AwerySettings.adultContent.state.value) {
				AwerySettings.AdultContent.SHOW -> true
				AwerySettings.AdultContent.HIDE -> !it.isNsfw
				AwerySettings.AdultContent.ONLY -> it.isNsfw
			}
		}.collect { watchModule ->
			_children += WatcherWatcher(
				ogExtension,
				watchModule, 
				watchModule.get<WatchModule>()!!, 
				media,
				coroutineScope
			).apply {
				coroutineScope.launch(Dispatchers.Default) { 
					load()
				}
			}
		}

		_isLoading.value = false
	}
}