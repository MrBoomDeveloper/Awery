package com.mrboomdev.awery.ui.tv

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mrboomdev.awery.data.settings.NicePreferences
import com.mrboomdev.awery.app.theme.TvTheme
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.generated.AwerySettings
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.ui.tv.screens.home.HomeScreen
import com.mrboomdev.awery.ui.tv.screens.media.MediaScreen
import com.mrboomdev.awery.ui.tv.screens.SettingsScreen
import com.mrboomdev.awery.util.NavUtils
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

class TvMainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			TvTheme {
				Navigation()
			}
		}
	}

	@Composable
	fun Navigation() {
		val navController = rememberNavController()

		NavHost(
			modifier = Modifier.background(Color.Black),
			navController = navController,
			startDestination = Screens.Home
		) {
			composable<Screens.Home> {
				HomeScreen(navController = navController)
			}

			composable<Screens.Media>(
				typeMap = mapOf(typeOf<CatalogMedia>() to NavUtils.getSerializableNavType<CatalogMedia>())
			) {
				MediaScreen(media = it.toRoute<Screens.Media>().media)
			}

			composable<Screens.Settings> {
				SettingsScreen(screen = NicePreferences.getSettingsMap())
			}
		}
	}
}

sealed class Screens {
	@Serializable
	data object Home
	@Serializable
	data object Settings
	@Serializable
	data class Media(val media: CatalogMedia): Screens()
}

class TvExperimentsActivity: Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		SettingsActivity.start(this, AwerySettings.get("experiments"))
		finish()
	}
}