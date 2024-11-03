package com.mrboomdev.awery.ui.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.tv.material3.MaterialTheme
import com.mrboomdev.awery.app.data.settings.NicePreferences
import com.mrboomdev.awery.ext.data.CatalogMedia
import com.mrboomdev.awery.ui.tv.screens.HomeScreen
import com.mrboomdev.awery.ui.tv.screens.HomeScreenArgs
import com.mrboomdev.awery.ui.tv.screens.MediaScreen
import com.mrboomdev.awery.ui.tv.screens.MediaScreenArgs
import com.mrboomdev.awery.ui.tv.screens.SettingsScreen
import com.mrboomdev.awery.ui.tv.screens.SettingsScreenArgs
import com.mrboomdev.awery.util.NavUtils
import kotlin.reflect.typeOf

class TvMainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		setContent {
			MaterialTheme {
				Navigation()
			}
		}
	}

	@Composable
	fun Navigation() {
		val navController = rememberNavController()
		NavHost(navController = navController, startDestination = HomeScreenArgs) {
			composable<HomeScreenArgs> {
				HomeScreen(navController = navController)
			}

			composable<MediaScreenArgs>(
				typeMap = mapOf(typeOf<CatalogMedia>() to NavUtils.getSerializableNavType<CatalogMedia>())
			) {
				MediaScreen(media = it.toRoute<MediaScreenArgs>().media)
			}

			composable<SettingsScreenArgs> {
				SettingsScreen(screen = NicePreferences.getSettingsMap())
			}
		}
	}
}