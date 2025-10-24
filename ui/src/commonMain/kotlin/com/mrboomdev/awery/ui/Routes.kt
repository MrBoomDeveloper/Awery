package com.mrboomdev.awery.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.data.settings.collectAsState
import com.mrboomdev.awery.extension.sdk.Preference
import com.mrboomdev.awery.extension.sdk.Video
import com.mrboomdev.awery.resources.*
import com.mrboomdev.awery.ui.screens.auth.AuthScreen
import com.mrboomdev.awery.ui.screens.browser.BrowserScreen
import com.mrboomdev.awery.ui.screens.extension.*
import com.mrboomdev.awery.ui.screens.home.HomeScreen
import com.mrboomdev.awery.ui.screens.intro.IntroScreen
import com.mrboomdev.awery.ui.screens.intro.IntroStep
import com.mrboomdev.awery.ui.screens.library.LibraryColumnScreen
import com.mrboomdev.awery.ui.screens.library.LibraryTabbedScreen
import com.mrboomdev.awery.ui.screens.media.MediaScreen
import com.mrboomdev.awery.ui.screens.notifications.NotificationsScreen
import com.mrboomdev.awery.ui.screens.player.PlayerScreen
import com.mrboomdev.awery.ui.screens.search.SearchScreen
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.screens.settings.pages.SettingsPages
import com.mrboomdev.navigation.core.TypeSafeNavigation
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Navigation = TypeSafeNavigation<Routes>()

@Serializable
sealed interface Routes {
	@Composable
	fun Content(contentPadding: PaddingValues)
	
	@Serializable
	data object Home: Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = HomeScreen(contentPadding = contentPadding)
	}

	@Serializable
	data object Search: Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = SearchScreen(contentPadding = contentPadding)
	}

	@Serializable
	data object Notifications: Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = NotificationsScreen(contentPadding = contentPadding)
	}

	@Serializable
	data object Library: Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = when(AwerySettings.libraryStyle.collectAsState().value) {
			AwerySettings.LibraryStyle.TABBED -> LibraryTabbedScreen(contentPadding = contentPadding)
			AwerySettings.LibraryStyle.COLUMN -> LibraryColumnScreen(contentPadding = contentPadding)
		}
	}

	@Serializable
	data class Settings(
		val initialPage: SettingsPages = SettingsPages.Main()
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = SettingsScreen(initialPage, contentPadding)
	}

	@Serializable
	data class Media(
		val extensionId: String,
		val extensionName: String?,
		val media: com.mrboomdev.awery.extension.sdk.Media
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = MediaScreen(this, contentPadding = contentPadding)
	}

	@Serializable
	data class Player(
		val video: Video,
		val title: String = video.title ?: video.url
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = PlayerScreen(this)
	}

	@Serializable
	data class Extension(
		val extensionId: String,
		val extensionName: String
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = ExtensionScreen(this, contentPadding = contentPadding)
	}

	@Serializable
	data class ExtensionFeed(
		val extensionId: String,
		val extensionName: String?,
		val feedId: String,
		val feedName: String
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = ExtensionFeedScreen(this, contentPadding = contentPadding)
	}

	@Serializable
	data class ExtensionSearch(
		val extensionId: String,
		val extensionName: String,
		val filters: List<Preference<*>>? = null
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = ExtensionSearchScreen(this, contentPadding = contentPadding)
	}

	@Serializable
	data class Intro(
		val step: IntroStep,
		val singleStep: Boolean
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = IntroScreen(this, contentPadding)
	}

	@Serializable
	data class Browser(
		val url: String
	): Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = BrowserScreen(url)
	}
	
	@Serializable
	data object Auth: Routes {
		@Composable
		override fun Content(
			contentPadding: PaddingValues
		) = AuthScreen(contentPadding = contentPadding)
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