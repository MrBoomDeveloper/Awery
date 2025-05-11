package com.mrboomdev.awery.ui

import androidx.compose.ui.graphics.Color

data class ThemeColors(
	val primary: Color,
	val background: Color
)

data class ThemeDeclaration(
	val dark: ThemeColors,
	val light: ThemeColors
)

object Themes {
	val Red = ThemeDeclaration(
		dark = ThemeColors(
			primary = Color.Red,
			background = Color(red = 20, green = 18, blue = 24)
		),

		light = ThemeColors(
			primary = Color.Red,
			background = Color(red = 254, green = 247, blue = 255)
		)
	)

	val Blue = ThemeDeclaration(
		dark = ThemeColors(
			primary = Color.Blue,
			background = Color(red = 20, green = 18, blue = 24)
		),

		light = ThemeColors(
			primary = Color.Blue,
			background = Color(red = 254, green = 247, blue = 255)
		)
	)

	val Green = ThemeDeclaration(
		dark = ThemeColors(
			primary = Color.Green,
			background = Color(red = 20, green = 18, blue = 24)
		),

		light = ThemeColors(
			primary = Color.Green,
			background = Color(red = 254, green = 247, blue = 255)
		)
	)
}