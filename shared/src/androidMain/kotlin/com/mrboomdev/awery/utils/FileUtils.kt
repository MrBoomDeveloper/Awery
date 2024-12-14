package com.mrboomdev.awery.utils

import android.content.Context

fun readAssets(
	context: Context,
	path: String
) = context.assets.open(path).readBytes().decodeToString()