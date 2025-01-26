package com.mrboomdev.awery.ui.utils

import androidx.compose.runtime.staticCompositionLocalOf
import com.dokar.sonner.ToasterState

val LocalToaster = staticCompositionLocalOf<ToasterState> { 
	throw NotImplementedError("No LocalToast was provided!")
}