package com.mrboomdev.awery.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.appcompat.graphics.drawable.StateListDrawableCompat
import androidx.core.content.ContextCompat
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class IconStateful(
	val active: String? = null,
	val inActive: String? = null,
	@DrawableRes var activeId: Int? = null,
	@DrawableRes var inActiveId: Int? = null,
	val names: Array<String> = arrayOf()
): Parcelable, java.io.Serializable {
	constructor(parcel: Parcel) : this(
		parcel.readString(),
		parcel.readString(),
		parcel.readByte().let { if(it.toInt() == 0) null else parcel.readInt() },
		parcel.readByte().let { if(it.toInt() == 0) null else parcel.readInt() },
		parcel.createStringArray() as Array<String>
	)

	override fun describeContents() = 0

	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(active)
		dest.writeString(inActive)

		if(activeId != null) {
			dest.writeByte(1)
			dest.writeInt(activeId!!)
		} else {
			dest.writeByte(0)
		}

		if(inActiveId != null) {
			dest.writeByte(1)
			dest.writeInt(inActiveId!!)
		} else {
			dest.writeByte(0)
		}

		dest.writeStringArray(names)
	}

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
					activeId = App.getResourceId(R.drawable::class.java, active)
					return activeId!!
				}
			}

			State.INACTIVE -> {
				if(inActiveId != null) {
					return inActiveId!!
				}

				if(inActive != null) {
					inActiveId = App.getResourceId(R.drawable::class.java, inActive)
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

	companion object {
		@Suppress("ConstPropertyName")
		const val serialVersionUID = 1L

		@JvmField
		val CREATOR = object : Parcelable.Creator<IconStateful> {

			override fun createFromParcel(parcel: Parcel): IconStateful {
				return IconStateful(parcel)
			}

			override fun newArray(size: Int): Array<IconStateful?> {
				return arrayOfNulls(size)
			}
		}
	}
}