package com.mrboomdev.awery.util.extensions

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.mrboomdev.awery.R
import com.mrboomdev.awery.app.App.Companion.getResourceId
import com.mrboomdev.safeargsnext.SafeArgsIntent
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.owner.SafeArgsService
import com.mrboomdev.safeargsnext.util.putSafeArgs
import org.jetbrains.annotations.Contract
import java.io.File
import kotlin.reflect.KClass

private const val TAG = "ContextExtensions"

val Context.configuration: Configuration
    get() = resources.configuration

inline fun <reified T> Context.startService(clazz: KClass<out SafeArgsService<T>>, args: T) {
    startService(Intent(this, clazz.java).apply {
        putSafeArgs(args as Any)
    })
}

inline fun <reified T : Service> Context.startService(
    action: String? = null,
    extras: Map<String, Any>? = null,
    data: Uri? = null
) {
    val intent = Intent(this, T::class.java)
    intent.action = action
    intent.data = data

    if(extras != null) {
        for(extra in extras) {
            intent.put(extra.key, extra.value)
        }
    }

    startService(intent)
}

fun <A> Context.startActivity(
    clazz: KClass<out SafeArgsActivity<A>>,
    args: A,
    action: String? = null,
    data: Uri? = null
) {
    startActivity(SafeArgsIntent(this, clazz, args).also {
        it.action = action
        it.data = data
    })
}

fun Context.startActivity(
    clazz: KClass<*>? = null,
    action: String? = null,
    extras: Map<String, Any>? = null,
    data: Uri? = null
) {
    val intent = Intent()
    intent.action = action
    intent.data = data

    if(clazz != null) {
        intent.component = ComponentName(this, clazz.java)
    }

    if(extras != null) {
        for((key, value) in extras) {
            intent.put(key, value)
        }
    }

    startActivity(intent)
}

val Context.screenWidth: Int
    get() = resources.displayMetrics.widthPixels

val Context.screenHeight: Int
    get() = resources.displayMetrics.heightPixels

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
    val clazz = if(name.startsWith("@mipmap/")) R.mipmap::class.java
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