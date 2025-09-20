package com.mrboomdev.awery.extension.loaders

import com.mrboomdev.awery.extension.sdk.Media

fun Media.getPoster() = poster ?: largePoster ?: banner
fun Media.getLargePoster() = largePoster ?: poster ?: banner
fun Media.getBanner() = banner ?: largePoster ?: poster