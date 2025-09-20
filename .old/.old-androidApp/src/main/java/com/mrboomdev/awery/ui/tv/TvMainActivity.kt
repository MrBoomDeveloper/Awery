package com.mrboomdev.awery.ui.tv

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mrboomdev.awery.app.theme.ThemeManager.setThemedContent
import com.mrboomdev.awery.data.settings.NicePreferences
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.data.settings.PlatformSetting
import com.mrboomdev.awery.ui.mobile.screens.settings.SettingsActivity
import com.mrboomdev.awery.ui.screens.settings.SettingsScreen
import com.mrboomdev.awery.ui.tv.screens.home.HomeScreen
import com.mrboomdev.awery.ui.tv.screens.media.MediaScreen
import com.mrboomdev.awery.util.NavUtils
import com.mrboomdev.awery.util.extensions.enableEdgeToEdge
import com.mrboomdev.awery.utils.readAssets
import com.mrboomdev.awery.utils.readClasspath
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.reflect.typeOf

class TvMainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setThemedContent {
			Navigation()
		}
	}

	@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3AdaptiveApi::class)
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
				val settings = remember {
					@Suppress("JSON_FORMAT_REDUNDANT")
					Json {
						decodeEnumsCaseInsensitive = true
						isLenient = true
					}.decodeFromString<PlatformSetting>(
						readClasspath("composeResources/com.mrboomdev.awery.generated/files/app_settings.json").toString(Charsets.UTF_8)
					).apply { restoreValues() }
				}

				SettingsScreen(
                    screen = settings
				)
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
		SettingsActivity.start(this, NicePreferences.getSettingsMap().findItem("experiments"))
		finish()
	}
}