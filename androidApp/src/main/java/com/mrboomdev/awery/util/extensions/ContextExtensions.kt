package com.mrboomdev.awery.util.extensions

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.annotation.AttrRes
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