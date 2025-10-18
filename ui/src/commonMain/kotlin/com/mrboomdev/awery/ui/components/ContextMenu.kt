package com.mrboomdev.awery.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.mrboomdev.awery.core.utils.collection.iterate
import com.mrboomdev.awery.core.utils.toInt
import com.mrboomdev.awery.resources.AweryFonts

internal data class ContextMenuItem(
	val text: String,
	val onClick: () -> Unit
)

class ContextMenuScope internal constructor() {
	internal val items = mutableListOf<ContextMenuItem>()
	
	fun item(
		text: String,
		onClick: () -> Unit
	) {
		items += ContextMenuItem(
			text = text,
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
			scope.items.iterate { (text, onClick) ->
				CompositionLocalProvider(
					LocalContentColor provides MaterialTheme.colorScheme.primary
				) {
					Text(
						modifier = Modifier
							.clip(RoundedCornerShape(4.dp))
							.clickable(onClick = {
								onClick()
								onDismissRequest()
							}).padding(horizontal = 16.dp, vertical = 10.dp)
							.fillMaxWidth(),
						fontFamily = AweryFonts.poppins,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurface,
						text = text
					)
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