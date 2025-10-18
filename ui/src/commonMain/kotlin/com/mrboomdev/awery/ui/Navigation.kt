package com.mrboomdev.awery.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.components.LocalToaster
import com.mrboomdev.awery.ui.screens.browser.BrowserScreen
import com.mrboomdev.awery.ui.screens.extension.*
import com.mrboomdev.awery.ui.screens.home.HomeScreen
import com.mrboomdev.awery.ui.screens.home.HomeViewModel
import com.mrboomdev.awery.ui.screens.intro.IntroScreen
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.library.LibraryColumnScreen
import com.mrboomdev.awery.ui.screens.library.LibraryTabbedScreen
import com.mrboomdev.awery.ui.screens.library.LibraryViewModel
import com.mrboomdev.awery.ui.screens.media.MediaScreen
import com.mrboomdev.awery.ui.screens.media.MediaScreenViewModel
import com.mrboomdev.awery.ui.screens.notifications.NotificationsScreen
import com.mrboomdev.awery.ui.screens.notifications.NotificationsViewModel
import com.mrboomdev.awery.ui.screens.player.PlayerScreen
import com.mrboomdev.awery.ui.screens.player.PlayerScreenViewModel
import com.mrboomdev.awery.ui.screens.search.SearchScreen
import com.mrboomdev.awery.ui.screens.search.SearchViewModel
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.utils.viewModel
import com.mrboomdev.navigation.core.TypeSafeNavigation
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Navigation = TypeSafeNavigation<Routes>()

@Serializable
sealed interface Routes {
	@Serializable
	data object Home: Routes {
		@Composable
		fun Content(
			viewModel: HomeViewModel = viewModel { HomeViewModel() }, 
			contentPadding: PaddingValues
		) = HomeScreen(viewModel, contentPadding)
	}

	@Serializable
	data object Search: Routes {
		@Composable
		fun Content(
			viewModel: SearchViewModel = viewModel { SearchViewModel() },
			contentPadding: PaddingValues
		) = SearchScreen(viewModel, contentPadding)
	}

	@Serializable
	data object Notifications: Routes {
		@Composable
		fun Content(
			viewModel: NotificationsViewModel = viewModel { NotificationsViewModel() },
			contentPadding: PaddingValues
		) = NotificationsScreen(viewModel, contentPadding)
	}

	@Serializable
	data object Library: Routes {
		@Composable
		fun Content(
			viewModel: LibraryViewModel = viewModel { LibraryViewModel() },
			contentPadding: PaddingValues
		) = when(AwerySettings.libraryStyle.collectAsState().value) {
			AwerySettings.LibraryStyle.TABBED -> LibraryTabbedScreen(viewModel, contentPadding)
			AwerySettings.LibraryStyle.COLUMN -> LibraryColumnScreen(viewModel, contentPadding)
		}
	}

	@Serializable
	data class Settings(
		val initialPage: SettingsPages = SettingsPages.Main()
	): Routes {
		@Composable
		fun Content(contentPadding: PaddingValues) = SettingsScreen(initialPage, contentPadding)
	}

	@Serializable
	data class Media(
		val extensionId: String,
		val extensionName: String?,
		val media: com.mrboomdev.awery.extension.sdk.Media
	): Routes {
		@Composable
		fun Content(
			viewModel: MediaScreenViewModel = viewModel { MediaScreenViewModel(this) },
			contentPadding: PaddingValues
		) = MediaScreen(this, viewModel, contentPadding)
	}

	@Serializable
	data class Player(
		val video: Video,
		val title: String = video.title ?: video.url
	): Routes {
		@Composable
		fun Content(
			viewModel: PlayerScreenViewModel = run {
				val navigation = Navigation.current()
				val toaster = LocalToaster.current
				
				viewModel {
					PlayerScreenViewModel(this@Player, navigation, toaster, it)
				}
			},
			contentPadding: PaddingValues
		) = PlayerScreen(this, viewModel)
	}

	@Serializable
	data class Extension(
		val extensionId: String,
		val extensionName: String
	): Routes {
		@Composable
		fun Content(
			viewModel: ExtensionScreenViewModel = viewModel { ExtensionScreenViewModel(extensionId) },
			contentPadding: PaddingValues
		) = ExtensionScreen(this, viewModel, contentPadding)
	}

	@Serializable
	data class ExtensionFeed(
		val extensionId: String,
		val extensionName: String?,
		val feedId: String,
		val feedName: String
	): Routes {
		@Composable
		fun Content(
			viewModel: ExtensionFeedScreenViewModel = run {
				val navigation = Navigation.current()
				val toaster = LocalToaster.current
				
				viewModel { ExtensionFeedScreenViewModel(this@ExtensionFeed, toaster, navigation) }
			},
			contentPadding: PaddingValues
		) = ExtensionFeedScreen(this, viewModel, contentPadding)
	}

	@Serializable
	data class ExtensionSearch(
		val extensionId: String,
		val extensionName: String,
		val filters: List<Preference<*>>? = null
	): Routes {
		@Composable
		fun Content(
			viewModel: ExtensionSearchScreenViewModel = viewModel { ExtensionSearchScreenViewModel(this) },
			contentPadding: PaddingValues
		) = ExtensionSearchScreen(this, viewModel, contentPadding)
	}

	@Serializable
	data class Intro(
		val step: IntroStep,
		val singleStep: Boolean
	): Routes {
		@Composable
		fun Content(
			contentPadding: PaddingValues
		) = IntroScreen(this, contentPadding)
	}

	@Serializable
	data class Browser(
		val url: String
	): Routes {
		@Composable
		fun Content(contentPadding: PaddingValues) = BrowserScreen(url)
	}
}

enum class MainRoutes(
	val title: StringResource,
	val icon: DrawableResource,
	val activeIcon: DrawableResource = icon,
	val route: Routes,
	val desktopOnly: Boolean = false
) {
	HOME(
		title = Res.string.home,
		icon = Res.drawable.ic_home_outlined,
		activeIcon = Res.drawable.ic_home_filled,
		route = Routes.Home
	),

	SEARCH(
		title = Res.string.search,
		icon = Res.drawable.ic_search,
		route = Routes.Search
	),

	NOTIFICATIONS(
		title = Res.string.notifications,
		icon = Res.drawable.ic_notifications_outlined,
		activeIcon = Res.drawable.ic_notifications_filled,
		route = Routes.Notifications
	),

	LIBRARY(
		title = Res.string.library,
		icon = Res.drawable.ic_collections_bookmark_outlined,
		activeIcon = Res.drawable.ic_collections_bookmark_filled,
		route = Routes.Library
	),

	SETTINGS(
		title = Res.string.settings,
		icon = Res.drawable.ic_settings_outlined,
		route = Routes.Settings(),
		desktopOnly = true
	),

	PROFILE(
		title = Res.string.account,
		icon = Res.drawable.ic_account_outlined,
		route = Routes.Intro(IntroStep.UserCreation, true),
		desktopOnly = true
	);

	fun getIcon(isActive: Boolean) = if(isActive) activeIcon else icon
}