package com.mrboomdev.awery.extension.loaders.watch

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.mrboomdev.awery.core.utils.NothingFoundException
import com.mrboomdev.awery.extension.loaders.Extensions.get
import com.mrboomdev.awery.extension.sdk.Extension
import com.mrboomdev.awery.extension.sdk.Media
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.modules.CatalogModule
import com.mrboomdev.awery.extension.sdk.modules.WatchModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class WatcherWatcher(
	private val ogExtension: String,
	val extension: Extension,
	val module: WatchModule,
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
		
		extension.get<CatalogModule>()!!.also { catalogModule ->
			try {
				if(extension.id == ogExtension) {
					_children.clear()
					
					_children += MediaWatcher(extension, media, coroutineScope).apply {
						coroutineScope.launch(Dispatchers.Default) {
							load()
						}
					}
					
					return@also
				}

				channelFlow { 
					for(title in listOf(media.title, *media.alternativeTitles.toTypedArray())) {
						launch(Dispatchers.IO) {
							send(catalogModule.search(
								catalogModule.getDefaultFilters().apply {
									first { it.role == Preference.Role.QUERY }.let {
										@Suppress("UNCHECKED_CAST")
										it as Preference<String>
									}.value = title
								}
							))
						}
					}
				}.onStart {
					_error.value = null
					_children.clear()
				}.onEach {
					if(it.items.isEmpty()) {
						throw NothingFoundException("No media was found!")
					}
				}.catch { 
					_error.value = it
				}.collect { results ->
					_children += results.items.map { media ->
						MediaWatcher(extension, media, coroutineScope).apply {
							coroutineScope.launch(Dispatchers.Default) {
								load()
							}
						}
					}.filterNot { a ->
						_children.any { b -> a.id == b.id }
					}
				}
			} catch(t: Throwable) {
				_error.value = t
			}
		}
		
		_isLoading.value = false
	}

	override val title: String
		get() = extension.name
	
	override val id: String
		get() = extension.id
}