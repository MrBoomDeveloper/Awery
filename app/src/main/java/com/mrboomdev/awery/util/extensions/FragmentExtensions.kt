package com.mrboomdev.awery.util.extensions

import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.annotation.AttrRes
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors
import com.mrboomdev.safeargsnext.SafeArgsIntent
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import kotlin.reflect.KClass

val Fragment.hasContext: Boolean
	get() = context != null

fun Fragment.resolveAttrColor(@AttrRes res: Int): Int {
	return MaterialColors.getColor(requireContext(), res, Color.BLACK)
}

fun <A> Fragment.startActivity(
	clazz: KClass<out SafeArgsActivity<A>>,
	args: A,
	action: String? = null,
	data: Uri? = null
) {
	startActivity(SafeArgsIntent(requireContext(), clazz, args).also {
		it.action = action
		it.data = data
	})
}

fun Fragment.startActivity(
	clazz: KClass<*>? = null,
	action: String? = null,
	extras: Map<String, Any>? = null,
	data: Uri? = null
) {
	val intent = Intent()
	intent.action = action
	intent.data = data

	if(clazz != null) {
		intent.component = ComponentName(requireContext(), clazz.java)
	}

	if(extras != null) {
		for((key, value) in extras) {
			intent.put(key, value)
		}
	}

	startActivity(intent)
}