package com.mrboomdev.awery.util.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewParent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type.displayCutout
import androidx.core.view.WindowInsetsCompat.Type.systemBars
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.R
import com.mrboomdev.awery.app.App.Companion.getMarkwon
import com.mrboomdev.awery.app.AweryLifecycle.Companion.runDelayed
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAlign

fun TextView.setMarkwon(markdown: String) {
    getMarkwon(context).setMarkdown(this, markdown)
}

fun <T : View> ViewGroup.addView(view: T, runAfterAdding: (view: T) -> Unit) {
    addView(view)
    runAfterAdding(view)
}

fun <T : View> ViewGroup.addView(view: T, index: Int, runAfterAdding: (view: T) -> Unit) {
    addView(view, index)
    runAfterAdding(view)
}

fun <T : View> ViewGroup.addView(view: T, width: Int, height: Int, runAfterAdding: (view: T) -> Unit) {
    addView(view, width, height)
    runAfterAdding(view)
}

inline fun <reified T : ViewGroup.LayoutParams> View.useLayoutParams(callback: (params: T) -> Unit) {
    val params = (layoutParams ?: throw NullPointerException("This view isn't attached to any parent!")) as T?
        ?: throw IllegalArgumentException("This view's layout params are of a different type!")

    callback(params)
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

var View.scale: Float
    get() = (scaleX + scaleY) / 2
    set(value) {
        scaleX = value
        scaleY = value
    }

val ViewBinding.context: Context
    get() = root.context

var View.weight: Float
    get() = (layoutParams as LinearLayout.LayoutParams).weight
    set(value) = useLayoutParams<LinearLayout.LayoutParams> { it.weight = value }

fun LinearLayout.addView(view: View, width: Int, height: Int, weight: Float) {
    addView(view, LinearLayout.LayoutParams(width, height, weight))
}

fun LinearLayoutCompat.addView(view: View, width: Int, height: Int, weight: Float) {
    addView(view, LinearLayoutCompat.LayoutParams(width, height, weight))
}

//-------------------//
//----- Popups ------//
//-------------------//

fun View.balloon(text: String, align: BalloonAlign) {
    // For some strange reason an balloon doesn't show up right after activity been created,
    // so we need to wait a little bit.

    runDelayed({
        Balloon.Builder(context)
            .setText(text)
            .setTextSize(14f)
            .setPaddingVertical(6)
            .setPaddingHorizontal(12)
            .setMaxWidthRatio(.8f)
            .setMaxWidth(100)
            .setCornerRadius(8f)
            .setArrowOrientation(
                when(align) {
                    BalloonAlign.TOP -> ArrowOrientation.BOTTOM
                    BalloonAlign.BOTTOM -> ArrowOrientation.TOP
                    BalloonAlign.START -> ArrowOrientation.END
                    BalloonAlign.END -> ArrowOrientation.START
                }
            )
            .setTextColor(context.resolveAttrColor(R.attr.colorSurface))
            .setBackgroundColor(context.resolveAttrColor(R.attr.colorOnSurface))
            .setLifecycleOwner(context.activity as AppCompatActivity?)
            .build().showAlign(align, this)
    }, 1)
}

//-------------------//
//----- Insets ------//
//-------------------//

val UI_INSETS = systemBars() or displayCutout()

/**
 * @param listener Return true to consume insets
 */
fun <T : ViewBinding> T.applyInsets(
    @WindowInsetsCompat.Type.InsetsType insets: Int,
    listener: (binding: T, insets: Insets) -> Boolean,
    parent: ViewParent? = null
) {
    var rootInsets = root.rootWindowInsets
    var latestParent: ViewParent? = parent

    while(rootInsets == null) {
        if(latestParent == null) break
        latestParent = latestParent.parent

        if(latestParent !is View) break
        rootInsets = latestParent.rootWindowInsets
    }

    if(rootInsets != null) {
        listener(this, WindowInsetsCompat.toWindowInsetsCompat(rootInsets, root).getInsets(insets))
    }

    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insetsCompat ->
        if(listener(this, insetsCompat.getInsets(insets))) WindowInsetsCompat.CONSUMED else insetsCompat
    }
}

/**
 * @param listener Return true to consume insets
 */
fun <T : View> T.applyInsets(
    @WindowInsetsCompat.Type.InsetsType insets: Int = UI_INSETS,
    listener: (view: T, insets: Insets) -> Boolean,
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

fun View.setPadding(horizontal: Int, vertical: Int) {
    setPadding(horizontal, vertical, horizontal, vertical)
}

//------------------//
//----- Margin -----//
//------------------//

private fun getMargins(view: View): MarginLayoutParams {
    val params = view.layoutParams ?: throw NullPointerException("This view isn't attached to any parent!")
    return if(params is MarginLayoutParams) params
    else throw IllegalStateException("Parent of this view does not support margins!")
}

fun View.setMargin(callback: MarginLayoutParams.() -> Unit) {
    useLayoutParams<MarginLayoutParams> {
        callback(it)
    }
}

fun View.setHorizontalMargin(margin: Int) {
    useLayoutParams<MarginLayoutParams> {
        it.rightMargin = margin
        it.leftMargin = margin
    }
}

fun View.setVerticalMargin(margin: Int) {
    useLayoutParams<MarginLayoutParams> {
        it.topMargin = margin
        it.bottomMargin = margin
    }
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