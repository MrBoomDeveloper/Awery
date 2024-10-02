package com.mrboomdev.awery.util.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.mrboomdev.awery.R
import org.jetbrains.annotations.Contract
import java.io.File

private const val TAG = "ContextExtensions"

fun Context.getCacheFile(path: String): File {
    return File(cacheDir, path)
}

fun Context.getFile(path: String): File {
    return File(filesDir, path)
}

fun Context.dpPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()
}

fun Context.spPx(sp: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
}

val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

val Context.activity: Activity
    get() {
        var context = this

        while(context is ContextWrapper) {
            if(context is Activity) {
                return context
            }

            context = context.baseContext
        }

        throw IllegalStateException("Context is not an Activity!")
    }

/**
 * Possible name param syntax:
 * <P>`my_awesome_icon` - Will return an icon from the drawable directory</P>
 *
 * `@mipmap/my_awesome_mipmap` - Will return an drawable from the mipmap directory
 *
 * `@color/my_color` - WIll return an [ColorDrawable] instance
 * @throws Resources.NotFoundException If no resource with such name was found
 * @author MrBoomDev
 */
@Contract(pure = true)
@Throws(Resources.NotFoundException::class)
fun Context.resolveDrawable(name: String): Drawable? {
    val clazz: Class<*> = if(name.startsWith("@mipmap/")) R.mipmap::class.java
    else if(name.startsWith("@color/")) R.color::class.java
    else R.drawable::class.java

    var res = name

    if(name.contains("/")) {
        res = name.substring(name.indexOf("/") + 1)
    }

    val id = getResourceId(clazz, res)

    if(clazz == R.color::class.java) {
        val color = ContextCompat.getColor(this, id)
        return ColorDrawable(color)
    }

    return ContextCompat.getDrawable(this, id)
}

fun getResourceId(type: Class<*>, res: String?): Int {
    if(res == null) return 0

    try {
        val field = type.getDeclaredField(res)
        field.isAccessible = true
        val result = field[null]

        if(result == null) {
            Log.e(TAG, "Resource id \"" + res + "\" was not initialized in \"" + type.name + "\"!")
            return 0
        }

        return result as Int
    } catch(e: NoSuchFieldException) {
        return 0
    } catch(e: IllegalAccessException) {
        throw IllegalStateException(
            "Generated resource id filed cannot be private! Check if the provided class is the R class", e)
    }
}

fun Context.resolveAttr(@AttrRes res: Int): TypedValue? {
    val typedValue = TypedValue()

    if(theme.resolveAttribute(res, typedValue, true)) {
        return typedValue
    }

    return null
}

fun Context.resolveAttrColor(@AttrRes res: Int): Int {
    return MaterialColors.getColor(this, res, Color.BLACK)
}