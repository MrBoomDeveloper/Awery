package com.mrboomdev.awery.data

import com.mrboomdev.awery.utils.annotations.AweryExperimentalApi

object AweryAppFilters {
	const val PROCESSOR_MANAGER = "AWERY.PROCESSOR"
	
	const val FEED_AUTOGENERATE = "AWERY.FEED.AUTOGENERATE"
	const val FIRST_FEED_LARGE = "AWERY.FILTER.FIRST_FEED_LARGE"
	
	@AweryExperimentalApi
	const val FEED_CONTINUE = "AWERY.FEED.CONTINUE"
	
	@AweryExperimentalApi
	const val FEED_BOOKMARKS = "AWERY.FEED.BOOKMARKS"
}