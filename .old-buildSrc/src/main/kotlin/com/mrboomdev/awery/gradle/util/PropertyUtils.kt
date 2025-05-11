package com.mrboomdev.awery.gradle.util

import org.gradle.api.provider.ListProperty

operator fun <T> ListProperty<T>.plusAssign(list: List<T>) = addAll(list)