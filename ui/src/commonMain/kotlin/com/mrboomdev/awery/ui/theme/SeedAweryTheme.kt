package com.mrboomdev.awery.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.Contrast
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import com.mrboomdev.awery.data.settings.AwerySettings

@Composable
expect fun SeedAweryTheme(
    seedColor: Color,
    content: @Composable () -> Unit
)

@Composable
fun seedColorScheme(
    seedColor: Color,
    isDark: Boolean = isDarkTheme(),
    style: PaletteStyle = PaletteStyle.Rainbow,
    contrastLevel: Double = Contrast.Default.value
): ColorScheme {
    return rememberDynamicMaterialThemeState(
        seedColor = seedColor,
        isDark = isDark,
        isAmoled = AwerySettings.amoledTheme.state.value,
        style = style,
        contrastLevel = contrastLevel,
        specVersion = ColorSpec.SpecVersion.SPEC_2025
    ).colorScheme
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SeedAweryThemeImpl(
    seedColor: Color,
    content: @Composable () -> Unit
) {
    val animationSpec: AnimationSpec<Color> = spring(stiffness = Spring.StiffnessLow)

    @Composable
    fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
        return animateColorAsState(this, animationSpec).value
    }

    val colorScheme = seedColorScheme(seedColor).let { colorScheme ->
        // Their amoled is not so black as we want so we do apply our own effects
        if(isDarkTheme() && AwerySettings.amoledTheme.state.value) {
            colorScheme.toAmoledColorScheme()
        } else colorScheme
    }.let { colorScheme ->
        colorScheme.copy(
            primary = colorScheme.primary.animate(animationSpec),
            primaryContainer = colorScheme.primaryContainer.animate(animationSpec),
            secondary = colorScheme.secondary.animate(animationSpec),
            secondaryContainer = colorScheme.secondaryContainer.animate(animationSpec),
            tertiary = colorScheme.tertiary.animate(animationSpec),
            tertiaryContainer = colorScheme.tertiaryContainer.animate(animationSpec),
            background = colorScheme.background.animate(animationSpec),
            surface = colorScheme.surface.animate(animationSpec),
            surfaceTint = colorScheme.surfaceTint.animate(animationSpec),
            surfaceBright = colorScheme.surfaceBright.animate(animationSpec),
            surfaceDim = colorScheme.surfaceDim.animate(animationSpec),
            surfaceContainer = colorScheme.surfaceContainer.animate(animationSpec),
            surfaceContainerHigh = colorScheme.surfaceContainerHigh.animate(animationSpec),
            surfaceContainerHighest = colorScheme.surfaceContainerHighest.animate(animationSpec),
            surfaceContainerLow = colorScheme.surfaceContainerLow.animate(animationSpec),
            surfaceContainerLowest = colorScheme.surfaceContainerLowest.animate(animationSpec),
            surfaceVariant = colorScheme.surfaceVariant.animate(animationSpec),
            error = colorScheme.error.animate(animationSpec),
            errorContainer = colorScheme.errorContainer.animate(animationSpec),
            onPrimary = colorScheme.onPrimary.animate(animationSpec),
            onPrimaryContainer = colorScheme.onPrimaryContainer.animate(animationSpec),
            onSecondary = colorScheme.onSecondary.animate(animationSpec),
            onSecondaryContainer = colorScheme.onSecondaryContainer.animate(animationSpec),
            onTertiary = colorScheme.onTertiary.animate(animationSpec),
            onTertiaryContainer = colorScheme.onTertiaryContainer.animate(animationSpec),
            onBackground = colorScheme.onBackground.animate(animationSpec),
            onSurface = colorScheme.onSurface.animate(animationSpec),
            onSurfaceVariant = colorScheme.onSurfaceVariant.animate(animationSpec),
            onError = colorScheme.onError.animate(animationSpec),
            onErrorContainer = colorScheme.onErrorContainer.animate(animationSpec),
            inversePrimary = colorScheme.inversePrimary.animate(animationSpec),
            inverseSurface = colorScheme.inverseSurface.animate(animationSpec),
            inverseOnSurface = colorScheme.inverseOnSurface.animate(animationSpec),
            outline = colorScheme.outline.animate(animationSpec),
            outlineVariant = colorScheme.outlineVariant.animate(animationSpec),
            scrim = colorScheme.scrim.animate(animationSpec)
        )
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography,
        content = content
    )
}