package com.mrboomdev.awery.ui.mobile.screens.player

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.WeakHashMap

object PlayerPip {
	const val ACTION = "ACTION"

	enum class Action {
		PLAY, PAUSE
	}

	private val callbacks = WeakHashMap<Context, (Action) -> Unit>()

	fun addCallback(context: Context, callback: (Action) -> Unit) {
		callbacks[context] = callback
	}

	class Receiver: BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			val value = Action.valueOf(intent.getStringExtra(ACTION)!!)

			for(callback in callbacks.values) {
				callback(value)
			}
		}
	}
}