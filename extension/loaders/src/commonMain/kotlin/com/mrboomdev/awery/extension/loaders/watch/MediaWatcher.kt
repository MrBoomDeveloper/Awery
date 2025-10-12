package com.mrboomdev.awery.extension.loaders.watch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.get
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MediaWatcher(
	val extension: Extension,
	val media: Media,
	private val coroutineScope: CoroutineScope
): WatcherNode.Variants {
	private val _children = mutableStateListOf<WatcherNode>()
	override val children: List<WatcherNode> = _children
	
	private val _isLoading = mutableStateOf(true)
	override val isLoading by _isLoading
	
	private val _error = mutableStateOf<Throwable?>(null)
	override val error by _error

	override suspend fun load() {
		_error.value = null
		_isLoading.value = true
		
		try {
			extension.get<WatchModule>()!!.watch(media).also {
				_children.clear()
				_error.value = null
			}.get({ video ->
				_children += object : WatcherNode.Video {
					override val video = video
				}
			}, { variants ->
				if(variants.items.isEmpty()) {
					throw NothingFoundException("No variants were found!")
				}
				
				_children += variants.items.map { 
					VariantWatcher(extension, it, coroutineScope).apply { 
						coroutineScope.launch(Dispatchers.Default) { 
							load()
						}
					}
				}
			})
		} catch(t: Throwable) {
			_error.value = t
		}
		
		_isLoading.value = false
	}

	override val title = media.title
	override val id = media.id
}