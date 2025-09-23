package com.mrboomdev.awery.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.account
import com.mrboomdev.awery.resources.home
import com.mrboomdev.awery.resources.ic_account_outlined
import com.mrboomdev.awery.resources.ic_collections_bookmark_filled
import com.mrboomdev.awery.resources.ic_collections_bookmark_outlined
import com.mrboomdev.awery.resources.ic_home_filled
import com.mrboomdev.awery.resources.ic_home_outlined
import com.mrboomdev.awery.resources.ic_notifications_filled
import com.mrboomdev.awery.resources.ic_notifications_outlined
import com.mrboomdev.awery.resources.ic_search
import com.mrboomdev.awery.resources.ic_settings_outlined
import com.mrboomdev.awery.resources.library
import com.mrboomdev.awery.resources.notifications
import com.mrboomdev.awery.resources.search
import com.mrboomdev.awery.resources.settings
import com.mrboomdev.awery.ui.screens.browser.BrowserScreen
import com.mrboomdev.awery.ui.screens.extension.ExtensionFeedScreen
import com.mrboomdev.awery.ui.screens.extension.ExtensionScreen
import com.mrboomdev.awery.ui.screens.extension.ExtensionSearchScreen
import com.mrboomdev.awery.ui.screens.intro.IntroScreen
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.main.HomePage
import com.mrboomdev.awery.ui.screens.main.LibraryPage
import com.mrboomdev.awery.ui.screens.main.MainScreen
import com.mrboomdev.awery.ui.screens.main.MainScreenViewModel
import com.mrboomdev.awery.ui.screens.main.NotificationsPage
import com.mrboomdev.awery.ui.screens.main.SearchPage
import com.mrboomdev.awery.ui.screens.media.MediaScreen
import com.mrboomdev.awery.ui.screens.player.PlayerScreen
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.awery.ui.utils.viewModel
import com.mrboomdev.navigation.core.TypeSafeNavigation
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Navigation = TypeSafeNavigation<Routes>()

sealed interface Routes {
	@Serializable
	data object Home: Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) {
			HomePage(viewModel(::MainScreenViewModel), contentPadding)
		}
	}

	@Serializable
	data object Search: Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) {
			SearchPage(viewModel(::MainScreenViewModel), contentPadding, "")
		}
	}

	@Serializable
	data object Notifications: Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) {
			NotificationsPage(contentPadding)
		}
	}

	@Serializable
	data object Library: Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) {
			LibraryPage(viewModel(::MainScreenViewModel), contentPadding)
		}
	}
	
	@Serializable
	data object Main: Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = MainScreen()
	}

	@Serializable
	data class Settings(
		val initialPage: SettingsPages = SettingsPages.Main()
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = SettingsScreen(initialPage)
	}

	@Serializable
	data class Media(
		val extensionId: String,
		val extensionName: String?,
		val media: com.mrboomdev.awery.extension.sdk.Media
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = MediaScreen(this)
	}

	@Serializable
	data class Player(
		val video: Video,
		val title: String = video.title ?: video.url
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = PlayerScreen(this)
	}

	@Serializable
	data class Extension(
		val extensionId: String,
		val extensionName: String
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = ExtensionScreen(this)
	}

	@Serializable
	data class ExtensionFeed(
		val extensionId: String,
		val extensionName: String,
		val feedId: String,
		val feedName: String
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = ExtensionFeedScreen(this)
	}

	@Serializable
	data class ExtensionSearch(
		val extensionId: String,
		val extensionName: String
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = ExtensionSearchScreen(this)
	}

	@Serializable
	data class Intro(
		val step: IntroStep,
		val singleStep: Boolean
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = IntroScreen(this)
	}

	@Serializable
	data class Browser(
		val url: String
	): Routes {
		@Composable
		override fun Content(contentPadding: PaddingValues) = BrowserScreen(url)
	}

	@Composable
	fun Content(contentPadding: PaddingValues)
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

internal fun getInitialRoute(): Routes {
	if(!AwerySettings.introDidWelcome.value) {
		return Routes.Intro(IntroStep.Welcome, singleStep = false)
	}

	if(!AwerySettings.introDidTheme.value) {
		return Routes.Intro(IntroStep.Theme, singleStep = false)
	}

	if(AwerySettings.username.value.isBlank()) {
		return Routes.Intro(IntroStep.UserCreation, singleStep = false)
	}

	return Routes.Main
}