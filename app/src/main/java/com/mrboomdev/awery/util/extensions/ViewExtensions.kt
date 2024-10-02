package com.mrboomdev.awery.util.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewParent
import android.view.WindowInsets
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.core.view.marginBottom
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.mrboomdev.awery.util.ui.ViewUtil
import com.mrboomdev.awery.util.ui.ViewUtil.InsetsUpdateListener

inline fun <reified T : ViewGroup.LayoutParams> View.useLayoutParams(callback: (params: T) -> Unit) {
    val params = layoutParams ?: throw NullPointerException("This view isn't attached to any parent!")
    callback.invoke(params as T)
    layoutParams = params
}

fun Fragment.dpPx(dp: Float): Int {
    return requireContext().dpPx(dp)
}

fun View.dpPx(dp: Float): Int {
    return context.dpPx(dp)
}

fun Fragment.spPx(sp: Float): Float {
    return requireContext().spPx(sp)
}

fun View.spPx(sp: Float): Float {
    return context.spPx(sp)
}

val ViewBinding.context: Context
    get() = root.context

//-------------------//
//----- Insets ------//
//-------------------//

val UI_INSETS = systemBars() or displayCutout()

/**
 * @param listener Return true to consume insets
 */
fun View.applyInsets(
    @WindowInsetsCompat.Type.InsetsType insets: Int,
    listener: (view: View, insets: Insets) -> Boolean,
    parent: ViewParent? = null
) {
    var rootInsets = rootWindowInsets
    var latestParent: ViewParent? = parent

    while(rootInsets == null) {
        if(latestParent == null) break
        latestParent = latestParent.parent

        if(latestParent !is View) break
        rootInsets = latestParent.rootWindowInsets
    }

    if(rootInsets != null) {
        listener(this, WindowInsetsCompat.toWindowInsetsCompat(rootInsets, this).getInsets(insets))
    }

    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insetsCompat ->
        if(listener(this, insetsCompat.getInsets(insets))) WindowInsetsCompat.CONSUMED else insetsCompat
    }
}

//-------------------//
//----- Tinting -----//
//-------------------//

fun ImageView.setImageTintColor(@ColorInt color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}

fun ImageView.setImageTintAttr(@AttrRes attr: Int) {
    val color = context.resolveAttrColor(attr)
    imageTintList = ColorStateList.valueOf(color)
}

fun ImageView.clearImageTint() {
    imageTintList = null
}

//-------------------//
//----- Padding -----//
//-------------------//

var View.leftPadding
    get() = paddingLeft
    set(value) = setPadding(value, paddingTop, paddingRight, paddingBottom)

var View.topPadding
    get() = paddingTop
    set(value) = setPadding(paddingLeft, value, paddingRight, paddingBottom)

var View.rightPadding
    get() = paddingRight
    set(value) = setPadding(paddingLeft, paddingTop, value, paddingBottom)

var View.bottomPadding
    get() = paddingBottom
    set(value) = setPadding(paddingLeft, paddingTop, paddingRight, value)

fun View.setHorizontalPadding(padding: Int) {
    setPadding(padding, paddingTop, padding, paddingBottom)
}

fun View.setVerticalPadding(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, padding)
}

fun View.setPadding(padding: Int) {
    setPadding(padding, padding, padding, padding)
}

//------------------//
//----- Margin -----//
//------------------//

private fun getMargins(view: View): MarginLayoutParams {
    val params = view.layoutParams ?: throw NullPointerException("This view isn't attached to any parent!")
    return if(params is MarginLayoutParams) params
    else throw IllegalStateException("Parent of this view does not support margins!")
}

var View.topMargin
    get() = getMargins(this).topMargin
    set(value) = useLayoutParams<MarginLayoutParams> { it.topMargin = value }

var View.bottomMargin
    get() = getMargins(this).bottomMargin
    set(value) = useLayoutParams<MarginLayoutParams> { it.bottomMargin = value }

var View.leftMargin
    get() = getMargins(this).leftMargin
    set(value) = useLayoutParams<MarginLayoutParams> { it.leftMargin = value }

var View.rightMargin
    get() = getMargins(this).rightMargin
    set(value) = useLayoutParams<MarginLayoutParams> { it.rightMargin = value }