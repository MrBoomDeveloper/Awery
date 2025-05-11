package com.mrboomdev.awery.app

import android.app.Application
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context

class ApplicationImpl: Application() {
    init {
        Awery.context = this
    }
}