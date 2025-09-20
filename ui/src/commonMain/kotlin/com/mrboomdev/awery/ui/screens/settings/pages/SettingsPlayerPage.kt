package com.mrboomdev.awery.ui.screens.settings.pages

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mrboomdev.awery.data.settings.AwerySettings
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.fill
import com.mrboomdev.awery.resources.fit
import com.mrboomdev.awery.resources.ic_fast_forward_outlined
import com.mrboomdev.awery.resources.ic_fit_screen_outlined
import com.mrboomdev.awery.resources.player
import com.mrboomdev.awery.resources.seek_time
import com.mrboomdev.awery.resources.seek_time_info
import com.mrboomdev.awery.resources.settings
import com.mrboomdev.awery.ui.screens.settings.SettingsDefaults
import com.mrboomdev.awery.ui.screens.settings.itemSetting
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsPlayerPage(
	modifier: Modifier,
	windowInsets: WindowInsets,
	onOpenPage: (SettingsPages) -> Unit,
	onBack: (() -> Unit)?
) {
	SettingsDefaults.page(
		modifier = modifier,
		windowInsets = windowInsets,
		onBack = onBack,
		title = { Text(stringResource(Res.string.player)) }
	) { contentPadding ->
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			contentPadding = contentPadding
		) {
			item("doubleTapSeekTime") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.playerDoubleTapSeek,
					range = 5..60,
					step = 10,
					icon = painterResource(Res.drawable.ic_fast_forward_outlined),
					title = stringResource(Res.string.seek_time),
					description = stringResource(Res.string.seek_time_info)
				)
			}
			
			item("fitMode") {
				SettingsDefaults.itemSetting(
					setting = AwerySettings.defaultPlayerFitMode,
					icon = painterResource(Res.drawable.ic_fit_screen_outlined),
					title = "Video scale mode",
					enumValues = {
						when(it) {
							AwerySettings.PlayerFitMode.FIT -> stringResource(Res.string.fit)
							AwerySettings.PlayerFitMode.CROP -> "Crop"
							AwerySettings.PlayerFitMode.FILL -> stringResource(Res.string.fill)
						}
					}
				)
			}
		}
	}
}