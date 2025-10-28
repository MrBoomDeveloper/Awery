package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.database.entity.DBRepository
import com.mrboomdev.awery.ui.screens.intro.steps.IntroThemeStep
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.utils.add
import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsPages {
	@Serializable
	class Main: SettingsPages {
		var current by mutableStateOf<SettingsPages?>(null)
		
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsMainPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onOpenPage = onOpenPage,
			current = current,
			onBack = onBack
		)
	}

	@Serializable
	data object Appearance: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsDefaults.page(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack,
			title = { Text("Appearance") }
		) { contentPadding ->
			Box(
				modifier = Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(contentPadding)
			) {
				IntroThemeStep.ChildContent(
					PaddingValues(horizontal = 18.dp).add(top = 8.dp)
				)
			}
		}
	}

	@Serializable
	data object Ui: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsUiPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack
		)
	}

	@Serializable
	data object Catalog: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsCatalogPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onOpenPage = onOpenPage,
			onBack = onBack
		)
	}

	@Serializable
	data object Library: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsLibraryPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onOpenPage = onOpenPage,
			onBack = onBack
		)
	}

	@Serializable
	data object Player: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsPlayerPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onOpenPage = onOpenPage,
			onBack = onBack
		)
	}

	@Serializable
	data object Extensions: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsExtensionsPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onOpenPage = onOpenPage,
			onBack = onBack
		)
	}
	
	@Serializable
	data class Extension(
		val id: String
	): SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsExtensionPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack,
			id = id
		)
	}
	
	@Serializable
	data class Repository(
		private val repository: DBRepository
	): SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsRepositoryPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack,
			onOpenPage = onOpenPage,
			repository = repository
		)
	}

	@Serializable
	data object Lists: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsListsPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack
		)
	}

	@Serializable
	data object Storage: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsStoragePage(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack,
			onOpenPage = onOpenPage
		)
	}

	@Serializable
	data object Advanced: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsAdvancedPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onOpenPage = onOpenPage,
			onBack = onBack
		)
	}

	@Serializable
	data object About: SettingsPages {
		@Composable
		override fun Content(
			modifier: Modifier,
			windowInsets: WindowInsets,
			onOpenPage: (SettingsPages) -> Unit,
			onBack: (() -> Unit)?
		) = SettingsAboutPage(
			modifier = modifier,
			windowInsets = windowInsets,
			onBack = onBack
		)
	}

	@Composable
	fun Content(
		modifier: Modifier,
		windowInsets: WindowInsets,
		onOpenPage: (SettingsPages) -> Unit,
		onBack: (() -> Unit)?
	)
}