package com.mrboomdev.awery.util.extensions

import android.content.res.ColorStateList

fun Int.toColorState(): ColorStateList {
	return ColorStateList.valueOf(this)
}