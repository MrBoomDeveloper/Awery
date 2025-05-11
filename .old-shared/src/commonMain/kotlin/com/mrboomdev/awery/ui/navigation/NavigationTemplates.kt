package com.mrboomdev.awery.ui.navigation

import com.mrboomdev.awery.data.AweryAppFilters
import com.mrboomdev.awery.ext.data.CatalogFeed
import com.mrboomdev.awery.generated.*
import com.mrboomdev.awery.platform.i18n

enum class NavigationTemplates {
	AWERY {
		override val experience: NavigationExperience
			get() = NavigationExperience(
				name = "Awery",
				
				topBar = listOf(
					NavigationExperience.Item(
						name = i18n(Res.string.search),
						inActiveIcon = Res.drawable.ic_search,
						route = NavigationRoute.Search
					),
					
					NavigationExperience.Item(
						name = i18n(Res.string.settings),
						inActiveIcon = Res.drawable.ic_settings_filled,
						route = NavigationRoute.Settings()
					)
				),
				
				navigationBar = listOf(
					NavigationExperience.Item(
						name = i18n(Res.string.home),
						activeIcon = Res.drawable.ic_home_filled,
						inActiveIcon = Res.drawable.ic_home_outlined,
						route = NavigationRoute.Feeds(listOf(
							CatalogFeed(
								managerId = AweryAppFilters.PROCESSOR_MANAGER, 
								feedId = AweryAppFilters.FEED_AUTOGENERATE, 
								title = ""
							)
						))
					)
				)
			)
	},
	
	DANTOTSU {
		override val experience: NavigationExperience
			get() = NavigationExperience(
				name = "Dantotsu",
				
				topBar = listOf(
					NavigationExperience.Item(
						name = i18n(Res.string.search),
						inActiveIcon = Res.drawable.ic_search,
						route = NavigationRoute.Search
					),
					
					NavigationExperience.Item(
						name = i18n(Res.string.settings),
						inActiveIcon = Res.drawable.ic_settings_filled,
						route = NavigationRoute.Settings()
					)
				),
				
				navigationBar = listOf(
					NavigationExperience.Item(
						name = i18n(Res.string.home),
						activeIcon = Res.drawable.ic_home_filled,
						inActiveIcon = Res.drawable.ic_home_outlined,
						route = NavigationRoute.Feeds(listOf(
							
						))
					),
					
					NavigationExperience.Item(
						name = i18n(Res.string.anime),
						activeIcon = Res.drawable.ic_movie_filled,
						inActiveIcon = Res.drawable.ic_movie_outlined,
						route = NavigationRoute.Feeds(emptyList())
					),
					
					NavigationExperience.Item(
						name = i18n(Res.string.manga),
						activeIcon = Res.drawable.ic_book_filled,
						inActiveIcon = Res.drawable.ic_book_outlined,
						route = NavigationRoute.Feeds(emptyList())
					),
					
					NavigationExperience.Item(
						name = i18n(Res.string.downloads),
						inActiveIcon = Res.drawable.ic_download,
						route = NavigationRoute.Feeds(emptyList())
					)
				)
			)
	};
	
	abstract val experience: NavigationExperience
}