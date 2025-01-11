package com.mrboomdev.awery.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mrboomdev.safeargsnext.owner.SafeArgsOwner
import com.mrboomdev.safeargsnext.util.putSafeArgs
import kotlin.reflect.KClass

/**
 * Build an implicit intent (It may become explicit, if you'll specify an component in the builder argument).
 */
fun buildIntent(
    action: String? = null,
    data: Uri? = null,
    type: String? = null,
    builder: (Intent.() -> Unit)? = null
) = buildIntentImpl(
    intent = Intent(),
    action = action,
    data = data,
    type = type,
    builder = builder
)

/**
 * Build an intent with an component.
 */
fun Context.buildIntent(
    clazz: KClass<*>,
    action: String? = null,
    data: Uri? = null,
    type: String? = null,
    builder: (Intent.() -> Unit)? = null
) = buildIntentImpl(
    intent = Intent(this, clazz.java),
    action = action,
    data = data,
    type = type,
    builder = builder
)

/**
 * Build an intent with an type-safe component.
 */
fun <A: SafeArgsOwner<B>, B : Any> Context.buildIntent(
    clazz: KClass<A>,
    args: B,
    action: String? = null,
    data: Uri? = null,
    type: String? = null,
    builder: (Intent.() -> Unit)? = null
) = buildIntent(
    clazz = clazz,
    action = action,
    data = data,
    type = type
) {
    putSafeArgs(args)
    builder?.invoke(this)
}

internal fun buildIntentImpl(
    intent: Intent,
    action: String?,
    data: Uri?,
    type: String?,
    builder: (Intent.() -> Unit)?
): Intent {
    intent.action = action
    intent.data = data
    intent.type = type
    builder?.invoke(intent)
    return intent
}