package com.mrboomdev.awery.resources

import androidx.compose.runtime.*
import androidx.compose.ui.text.font.*
import org.jetbrains.compose.resources.*

/**
 * An object that provides access to the Awery fonts.
 */
object AweryFonts {
    @get:Composable
    val poppins: FontFamily
        get() = FontFamily(
            FontCompat(Res.font.Poppins_Black, FontWeight.Black),
            FontCompat(Res.font.Poppins_Bold, FontWeight.Bold),
            FontCompat(Res.font.Poppins_ExtraBold, FontWeight.ExtraBold),
            FontCompat(Res.font.Poppins_ExtraLight, FontWeight.ExtraLight),
            FontCompat(Res.font.Poppins_Light, FontWeight.Light),
            FontCompat(Res.font.Poppins_Medium, FontWeight.Medium),
            FontCompat(Res.font.Poppins_Regular, FontWeight.Normal),
            FontCompat(Res.font.Poppins_Thin, FontWeight.Thin),
            FontCompat(Res.font.Poppins_SemiBold, FontWeight.SemiBold)
        )
}