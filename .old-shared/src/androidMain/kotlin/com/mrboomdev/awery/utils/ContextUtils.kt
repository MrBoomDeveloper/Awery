package com.mrboomdev.awery.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.core.content.ContextCompat

fun Context.getPackageUri(packageName: String = getPackageName()): Uri =
    Uri.parse("package:$packageName")

fun Context.hasPermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.dpPx(dp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).toInt()

fun Context.spPx(sp: Float) =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

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

        throw UnsupportedOperationException("Context is not an Activity!")
    }