package com.mrboomdev.awery.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mrboomdev.awery.ui.routes.BaseRoute

/**
 * An tv version of the [AweryRoot] with colors of [androidx.compose.material3.MaterialTheme]
 * being applied to [androidx.tv.material3.MaterialTheme]
 */
@Composable
internal actual fun AweryRootImpl(
	content: @Composable () -> Unit
) {
	AweryRootAndroid {
		androidx.tv.material3.MaterialTheme(
			colorScheme = androidx.tv.material3.MaterialTheme.colorScheme.copy(
				primary = MaterialTheme.colorScheme.primary,
				onPrimary = MaterialTheme.colorScheme.onPrimary,
				
				primaryContainer = MaterialTheme.colorScheme.primaryContainer,
				onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer,
				
				inversePrimary = MaterialTheme.colorScheme.inversePrimary,
				inverseOnSurface = MaterialTheme.colorScheme.inverseOnSurface,
				inverseSurface = MaterialTheme.colorScheme.inverseSurface,
				
				secondary = MaterialTheme.colorScheme.secondary,
				onSecondary = MaterialTheme.colorScheme.onSecondary,
				
				secondaryContainer = MaterialTheme.colorScheme.secondaryContainer,
				onSecondaryContainer = MaterialTheme.colorScheme.onSecondaryContainer,
				
				tertiary = MaterialTheme.colorScheme.tertiary,
				onTertiary = MaterialTheme.colorScheme.onTertiary,
				
				tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer,
				onTertiaryContainer = MaterialTheme.colorScheme.onTertiaryContainer,
				
				background = MaterialTheme.colorScheme.background,
				onBackground = MaterialTheme.colorScheme.onBackground,
				
				surface = MaterialTheme.colorScheme.surface,
				onSurface = MaterialTheme.colorScheme.onSurface,
				
				surfaceTint = MaterialTheme.colorScheme.surfaceTint,
				
				surfaceVariant = MaterialTheme.colorScheme.surfaceVariant,
				onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
				
				error = MaterialTheme.colorScheme.error,
				onError = MaterialTheme.colorScheme.onError,
				
				errorContainer = MaterialTheme.colorScheme.errorContainer,
				onErrorContainer = MaterialTheme.colorScheme.onErrorContainer,
				
				scrim = MaterialTheme.colorScheme.scrim
			),
			
			content = content
		)
	}
}