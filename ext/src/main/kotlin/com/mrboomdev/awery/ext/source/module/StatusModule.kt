package com.mrboomdev.awery.ext.source.module

import com.mrboomdev.awery.ext.data.CatalogMedia

interface StatusModule: Module {
	fun onStatus(status: Status)
	
	sealed class Status(val startTime: Long) {
		class Offline(
			startTime: Long
		): Status(startTime)
		
		sealed class Online(
			startTime: Long
		): Status(startTime)
		
		class Watching(
			startTime: Long, 
			val media: CatalogMedia
		): Online(startTime)
	}
}