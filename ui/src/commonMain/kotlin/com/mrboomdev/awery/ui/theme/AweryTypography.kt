package com.mrboomdev.awery.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.mrboomdev.awery.resources.AweryFonts

@get:Composable
internal val AweryTypography get() = MaterialTheme.typography.copy(
    displayLarge = MaterialTheme.typography.displayLarge.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),
    
    displayMedium = MaterialTheme.typography.displayMedium.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),
    
    displaySmall = MaterialTheme.typography.displaySmall.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),

    headlineLarge = MaterialTheme.typography.headlineLarge.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),

    headlineMedium = MaterialTheme.typography.headlineMedium.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),

    headlineSmall = MaterialTheme.typography.headlineSmall.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),
    
    titleLarge = MaterialTheme.typography.titleLarge.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),

    titleMedium = MaterialTheme.typography.titleMedium.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    ),

    titleSmall = MaterialTheme.typography.titleSmall.copy(
        fontFamily = AweryFonts.poppins,
        fontWeight = FontWeight.SemiBold
    )
)