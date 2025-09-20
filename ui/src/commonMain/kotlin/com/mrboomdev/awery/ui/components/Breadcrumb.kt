package com.mrboomdev.awery.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@Composable
fun Breadcrumb(
	modifier: Modifier = Modifier,
	contentPadding: PaddingValues = PaddingValues(0.dp),
	scrollState: ScrollState = rememberScrollState(),
	separator: @Composable () -> Unit = {
		Text(" > ")
	},
	scope: @Composable BreadcrumbScope.() -> Unit
) {
	Row(
		modifier = modifier
			.horizontalScroll(scrollState)
			.padding(contentPadding),
		verticalAlignment = Alignment.CenterVertically
	) {
		CompositionLocalProvider(
			LocalTextStyle provides LocalTextStyle.current.copy(
				color = MaterialTheme.colorScheme.primary
			)
		) {
			scope(object : BreadcrumbScope {
				@Composable
				override fun item(
					title: @Composable (() -> Unit),
					onClick: () -> Unit
				) {
					Box(
						modifier = Modifier
							.clip(RoundedCornerShape(8.dp))
							.clickable(onClick = onClick)
					) {
						title()
					}
				}

				@Composable
				override fun separator() {
					separator.invoke()
				}
			})
		}
	}
}

interface BreadcrumbScope {
	@Composable
	fun item(
		title: @Composable () -> Unit,
		onClick: () -> Unit
	)
	
	@Composable
	fun separator()
}