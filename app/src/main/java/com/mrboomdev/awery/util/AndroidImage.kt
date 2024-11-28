package com.mrboomdev.awery.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.mrboomdev.awery.ext.util.Image

class AndroidImage: Image {
	private val drawable: Drawable?
	private val res: Int?

	constructor(drawable: Drawable) {
		this.drawable = drawable
		this.res = null
	}

	constructor(@DrawableRes res: Int) {
		this.drawable = null
		this.res = res
	}

	fun getDrawable(context: Context): Drawable {
		if(res != null) {
			return ContextCompat.getDrawable(context, res)!!
		}

		return drawable!!
	}
}