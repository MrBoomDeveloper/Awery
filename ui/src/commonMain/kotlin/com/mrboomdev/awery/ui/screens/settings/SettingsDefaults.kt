package com.mrboomdev.awery.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mrboomdev.awery.data.settings.*
import com.mrboomdev.awery.resources.Res
import com.mrboomdev.awery.resources.ic_back
import com.mrboomdev.awery.ui.components.IconButton
import com.mrboomdev.awery.ui.theme.isAmoledTheme
import com.mrboomdev.awery.ui.utils.thenIf
import org.jetbrains.compose.resources.painterResource
import kotlin.enums.enumEntries

object SettingsDefaults {
	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun page(
		modifier: Modifier,
		windowInsets: WindowInsets,
		title: @Composable () -> Unit,
		onBack: (() -> Unit)?,
		fab: @Composable () -> Unit = {},
		content: @Composable (PaddingValues) -> Unit
	) {
		val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

		Scaffold(
			modifier = modifier.nestedScroll(topBarBehavior.nestedScrollConnection),
			contentWindowInsets = windowInsets,
			containerColor = Color.Transparent,
			floatingActionButton = fab,
			content = content,
			topBar = {
				TopAppBar(
					scrollBehavior = topBarBehavior,
					windowInsets = windowInsets.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
					title = title,

					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = Color.Transparent,
						scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.let {
							if(isAmoledTheme()) it.copy(alpha = .9f) else it
						}
					),

					navigationIcon = {
						onBack?.also {
							IconButton(
								padding = 6.dp,
								painter = painterResource(Res.drawable.ic_back),
								contentDescription = null,
								onClick = { it() }
							)
						}
					}
				)
			}
		)
	}
	
	@Composable
	fun item(
		modifier: Modifier = Modifier,
		contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
		icon: Painter? = null,
		title: String,
		description: String? = null,
		enumValues: List<Pair<Enum<*>, String>>? = null,
		onClick: (() -> Unit)? = null,
		range: IntRange? = null,
		step: Int = 1,
		checked: Boolean = false,
		dialog: (@Composable DialogScope.() -> Unit)? = null,
		content: @Composable () -> Unit = {},
		setting: Setting<*>? = null
	) {
		if(setting != null) {
			Setting(
				modifier = modifier
					.clip(RoundedCornerShape(32.dp))
					.fillMaxWidth(),
				contentPadding = contentPadding,

				icon = icon?.let {{
					Icon(
						modifier = Modifier.size(24.dp),
						tint = MaterialTheme.colorScheme.primary,
						painter = it,
						contentDescription = null
					)
				}},

				title = { Text(title) },
				description = description?.let {{ Text(it) }},
				range = range,
				step = step,
				setting = setting,
				enumValues = enumValues
			)
		} else {
			var showDialog by rememberSaveable { mutableStateOf(false) }

			val dialogScope = remember(dialog) {
				if(dialog == null) return@remember null

				object : DialogScope {
					override fun dismiss() {
						showDialog = false
					}
				}
			}

			if(showDialog) {
				dialog!!.invoke(dialogScope!!)
			}

			CompositionLocalProvider(
				LocalContentColor provides if(checked) {
					MaterialTheme.colorScheme.surfaceContainerLowest
				} else LocalContentColor.current
			) {
				Setting(
					modifier = modifier
						.clip(RoundedCornerShape(32.dp))
						.thenIf(checked) { background(MaterialTheme.colorScheme.onBackground) }
						.fillMaxWidth()
						.thenIf(onClick != null) { clickable(onClick = onClick!!) }
						.thenIf(dialog != null) { clickable { showDialog = true } },
					contentPadding = contentPadding,

					icon = icon?.let {{
						Icon(
							modifier = Modifier.size(24.dp),
							tint = if(checked) { LocalContentColor.current } else MaterialTheme.colorScheme.primary,
							painter = it,
							contentDescription = null
						)
					}},

					title = {
						Text(
							color = if(checked) { LocalContentColor.current } else Color.Unspecified,
							text = title
						)
					},
					
					description = description?.let {{ 
						Text(
							color = if(checked) { LocalContentColor.current } else Color.Unspecified,
							text = it
						) 
					}}
				) {
					content()
				}
			}
		}
	}

	@Composable
	fun itemCustom(
		modifier: Modifier = Modifier,
		icon: (@Composable () -> Unit)? = null,
		title: @Composable () -> Unit,
		description: (@Composable () -> Unit)? = null,
		onClick: (() -> Unit)? = null,
		checked: Boolean = false,
		contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
		content: (@Composable () -> Unit)? = null
	) {
		CompositionLocalProvider(
			LocalContentColor provides if(checked) {
				MaterialTheme.colorScheme.surfaceContainerLowest
			} else LocalContentColor.current
		) {
			Setting(
				modifier = Modifier
					.clip(RoundedCornerShape(32.dp))
					.then(modifier)
					.thenIf(checked) { background(MaterialTheme.colorScheme.onBackground) }
					.fillMaxWidth()
					.thenIf(onClick != null) { clickable(onClick = onClick!!) },
				contentPadding = contentPadding,

				icon = icon,
				title = title,
				description = description,
				content = content
			)
		}
	}
	
	interface DialogScope {
		fun dismiss()
	}
}

@Composable
fun SettingsDefaults.itemSetting(
	icon: Painter? = null,
	title: String,
	description: String? = null,
	setting: StringSetting
) = item(
	icon = icon,
	title = title,
	description = description,
	setting = setting
)

@Composable
fun SettingsDefaults.itemSetting(
	modifier: Modifier = Modifier,
	contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
	icon: Painter? = null,
	title: String,
	description: String? = null,
	setting: BooleanSetting
) = item(
	modifier = modifier,
	contentPadding = contentPadding,
	icon = icon,
	title = title,
	description = description,
	setting = setting
)

@Composable
fun SettingsDefaults.itemSetting(
	icon: Painter? = null,
	title: String,
	description: String? = null,
	range: IntRange,
	step: Int,
	setting: IntSetting
) = item(
	icon = icon,
	title = title,
	description = description,
	range = range,
	step = step,
	setting = setting
)

@Composable
inline fun <reified T: Enum<T>> SettingsDefaults.itemSetting(
	contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
	icon: Painter? = null,
	title: String,
	description: String? = null,
	enumValues: (T) -> String,
	setting: EnumSetting<T>
) = item(
	contentPadding = contentPadding,
	icon = icon,
	title = title,
	description = description,
	setting = setting,
	enumValues = buildList {
		enumEntries<T>().forEach {
			add(it to enumValues(it))
		}
	}
)

@Composable
fun SettingsDefaults.itemClickable(
	contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
	icon: Painter? = null,
	title: String,
	description: String? = null,
	checked: Boolean = false,
	onClick: () -> Unit
) = item(
	contentPadding = contentPadding,
	icon = icon,
	title = title,
	description = description,
	checked = checked,
	onClick = onClick
)

@Composable
fun SettingsDefaults.itemDialog(
	modifier: Modifier = Modifier,
	icon: Painter? = null,
	title: String,
	description: String? = null,
	contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
	content: @Composable () -> Unit = {},
	dialog: @Composable SettingsDefaults.DialogScope.() -> Unit
) = item(
	modifier = modifier,
	icon = icon,
	title = title,
	description = description,
	contentPadding = contentPadding,
	content = content,
	dialog = dialog
)