package com.mrboomdev.awery.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mrboomdev.awery.core.utils.collection.iterate
import com.mrboomdev.awery.core.utils.toInt
import com.mrboomdev.awery.resources.AweryFonts

internal data class ContextMenuItem(
	val icon: (@Composable () -> Painter)?,
	val text: @Composable () -> String,
	val onClick: () -> Unit
)

class ContextMenuScope internal constructor() {
	internal val items = mutableListOf<ContextMenuItem>()
	
	fun item(
		text: @Composable () -> String,
		onClick: () -> Unit
	) {
		items += ContextMenuItem(
			text = text,
			icon = null,
			onClick = onClick
		)
	}

	fun item(
		icon: @Composable () -> Painter,
		text: @Composable () -> String,
		onClick: () -> Unit
	) {
		items += ContextMenuItem(
			text = text,
			icon = icon,
			onClick = onClick
		)
	}
}

/**
 * Animated version of the [ContextMenu]
 */
@Composable
fun ContextMenu(
	isVisible: Boolean,
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	content: ContextMenuScope.() -> Unit
) {
	val scale by animateFloatAsState(isVisible.toInt().toFloat(), spring(
		dampingRatio = Spring.DampingRatioLowBouncy,
		stiffness = Spring.StiffnessHigh
	))
	
	if(scale > 0f) {
		ContextMenu(
			modifier = modifier.scale(scale),
			onDismissRequest = onDismissRequest,
			content = content
		)
	}
}

@Composable
fun ContextMenu(
	onDismissRequest: () -> Unit,
	modifier: Modifier = Modifier,
	content: ContextMenuScope.() -> Unit
) {
	val scope = remember(content) { ContextMenuScope().apply(content) }
	val shape = RoundedCornerShape(8.dp)
	
	Popup(
		onDismissRequest = onDismissRequest,
		properties = PopupProperties(
			focusable = true
		)
	) {
		Column(
			modifier = modifier
				.clip(shape)
				.background(MaterialTheme.colorScheme.surfaceContainerHighest)
				.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .05f))
				.border(.5.dp, MaterialTheme.colorScheme.surfaceContainerLowest, shape)
				.width(175.dp)
				.verticalScroll(rememberScrollState())
				.padding(vertical = 2.dp)
		) {
			scope.items.iterate { (icon, text, onClick) ->
				Surface(
					color = Color.Transparent,
					contentColor = MaterialTheme.colorScheme.primary,
					onClick = {
						onClick()
						onDismissRequest()
					}
				) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp),
						
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(16.dp)
					) {
						if(icon != null) {
							Icon(
								modifier = Modifier.size(24.dp),
								painter = icon(),
								contentDescription = null
							)
						}
						
						Text(
							modifier = Modifier
								.clip(RoundedCornerShape(4.dp))
								.padding(vertical = 10.dp)
								.fillMaxWidth(),
							fontFamily = AweryFonts.poppins,
							style = MaterialTheme.typography.bodySmall,
							color = MaterialTheme.colorScheme.onSurface,
							text = text()
						)
					}
				}

				if(hasNext()) {
					HorizontalDivider(
						modifier = Modifier.alpha(.5f),
						color = MaterialTheme.colorScheme.secondaryContainer
					)
				}
			}
		}
	}
}