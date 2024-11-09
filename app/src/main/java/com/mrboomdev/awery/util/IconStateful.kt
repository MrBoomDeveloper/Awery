package com.mrboomdev.awery.util

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.graphics.drawable.StateListDrawableCompat
import androidx.core.content.ContextCompat
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.getResourceId
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class IconStateful(
	val active: String? = null,
	val inActive: String? = null,
	@DrawableRes var activeId: Int? = null,
	@DrawableRes var inActiveId: Int? = null,
	val names: Array<String> = arrayOf()
) {

	@DrawableRes
	fun getResourceId(state: State): Int {
		return getResourceIdImpl(state) ?: when(state) {
			State.ACTIVE -> getResourceIdImpl(State.INACTIVE)
			State.INACTIVE -> getResourceIdImpl(State.ACTIVE)
		}!!
	}

	private fun getResourceIdImpl(state: State): Int? {
		when(state) {
			State.ACTIVE -> {
				if(activeId != null) {
					return activeId!!
				}

				if(active != null) {
					activeId = getResourceId<R.drawable>(active)
					return activeId!!
				}
			}

			State.INACTIVE -> {
				if(inActiveId != null) {
					return inActiveId!!
				}

				if(inActive != null) {
					inActiveId = getResourceId<R.drawable>(inActive)
					return inActiveId!!
				}
			}
		}

		return null
	}

	fun getDrawable(context: Context): Drawable {
		return StateListDrawableCompat().apply {
			addState(intArrayOf(android.R.attr.state_checked), getDrawable(context, State.ACTIVE))
			addState(intArrayOf(), getDrawable(context, State.INACTIVE))
		}
	}

	fun getDrawable(context: Context, state: State): Drawable? {
		return ContextCompat.getDrawable(context, getResourceId(state))
	}

	enum class State {
		ACTIVE, INACTIVE
	}
}