package com.mrboomdev.awery.ext

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import com.mrboomdev.awery.ext.util.Image

class AndroidImage: Image {
	private val res: Int?
	private val resName: String?
	private val drawable: Drawable?

	constructor(@DrawableRes res: Int) {
		this.res = res
		this.resName = null
		this.drawable = null
	}

	constructor(resName: String) {
		this.res = null
		this.resName = resName
		this.drawable = null
	}

	constructor(drawable: Drawable) {
		this.res = null
		this.resName = null
		this.drawable = drawable
	}
}