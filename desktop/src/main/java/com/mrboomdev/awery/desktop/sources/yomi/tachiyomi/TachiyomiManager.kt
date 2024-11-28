package com.mrboomdev.awery.desktop.sources.yomi.tachiyomi

import com.mrboomdev.awery.desktop.sources.yomi.YomiManager

class TachiyomiManager: YomiManager<TachiyomiSource>() {
	override val id = ID
	override val name = "Tachiyomi"

	companion object {
		const val ID = "TACHIYOMI_KOTLIN"
	}
}