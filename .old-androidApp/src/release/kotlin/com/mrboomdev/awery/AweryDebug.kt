package com.mrboomdev.awery

import com.mrboomdev.awery.R as RealR

object AweryDebug {
	@Suppress("ClassName", "PropertyName")
	object R {
		object drawable {
			private val debug_stub
				get() = RealR.drawable.debug_stub

			val sample_banner = debug_stub
			val sample_poster = debug_stub
		}
	}
}