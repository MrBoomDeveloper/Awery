package com.mrboomdev.awery.core

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
private var androidContext: Context? = null

var Awery.context: Context
    get() = androidContext!!
    set(value) { androidContext = value }