package com.mrboomdev.awery.util.extensions

import android.content.res.ColorStateList

fun Int.toColorState(): ColorStateList {
	return ColorStateList.valueOf(this)
}

operator fun Int?.plus(string: String): String {
	return toString() + string
}