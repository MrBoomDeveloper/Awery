package com.mrboomdev.awery.data

import androidx.compose.runtime.Composable
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.ui.utils.Tab

val DEFAULT_APP_TABS_GROUP = listOf(
	AppTabs.HOME,
	AppTabs.NEWS,
	AppTabs.NOTIFICATIONS,
	AppTabs.LIBRARY
)

enum class AppTabs(
	val tab: Tab,
	val content: @Composable () -> Unit
) {
	HOME(
		tab = Tab(
			title = Res.string.home,
			activeIcon = Res.drawable.ic_home_filled,
			inActiveIcon = Res.drawable.ic_home_outlined
		),
		
		content = {
			
		}
	),
	
	LIBRARY(
		tab = Tab(
			title = Res.string.library,
			activeIcon = Res.drawable.ic_home_filled,
			inActiveIcon = Res.drawable.ic_home_outlined
		),
		
		content = {
			
		}
	),
	
	SEARCH(
		tab = Tab(
			title = Res.string.search,
			activeIcon = Res.drawable.ic_home_filled,
			inActiveIcon = Res.drawable.ic_home_outlined
		),
		
		content = {
			
		}
	),
	
	NEWS(
		tab = Tab(
			title = Res.string.home,
			activeIcon = Res.drawable.ic_home_filled,
			inActiveIcon = Res.drawable.ic_home_outlined
		),
		
		content = {
			
		}
	),
	
	NOTIFICATIONS(
		tab = Tab(
			title = Res.string.notifications,
			activeIcon = Res.drawable.ic_home_filled,
			inActiveIcon = Res.drawable.ic_home_outlined
		),
		
		content = {
			
		}
	),
	
	DOWNLOADS(
		tab = Tab(
			title = Res.string.downloads,
			activeIcon = Res.drawable.ic_home_filled,
			inActiveIcon = Res.drawable.ic_home_outlined
		),
		
		content = {
			
		}
	)
}