package com.mrboomdev.awery

import android.app.Application
import com.mrboomdev.awery.platform.Platform
import com.mrboomdev.awery.platform.init
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App: Application() {
	init {
		Platform.attachBaseContext(this)
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreate() {
		super.onCreate()
		
		GlobalScope.launch(Dispatchers.Default) { 
			Platform.init()
		}
	}
}