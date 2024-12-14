package com.mrboomdev.awery.util.extensions

import android.content.Intent

fun Intent.toChooser(title: CharSequence) = Intent.createChooser(this, title)