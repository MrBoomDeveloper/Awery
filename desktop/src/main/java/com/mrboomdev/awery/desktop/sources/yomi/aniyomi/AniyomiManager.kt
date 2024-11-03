package com.mrboomdev.awery.desktop.sources.yomi.aniyomi

import com.mrboomdev.awery.desktop.sources.yomi.YomiManager

class AniyomiManager: YomiManager<AniyomiSource>() {
	override val id = ID
	override val name = "Aniyomi"

	companion object {
		const val ID = "ANIYOMI_KOTLIN"
	}
}