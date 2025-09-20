package com.mrboomdev.awery.extension.loaders.watch

sealed interface WatcherNode {
	val title: String
	val id: String
	
	val isLoading: Boolean
		get() = false
	
	val error: Throwable?
		get() = null

	interface Variants: WatcherNode {
		val children: List<WatcherNode>
		suspend fun load()
	}

	interface Video: WatcherNode {
		val video: com.mrboomdev.awery.extension.sdk.Video
		
		override val title: String
			get() = video.title ?: video.url
		
		override val id: String
			get() = video.url
	}
}