package com.mrboomdev.awery.platform.android

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.mrboomdev.awery.utils.SerializableRequired
import com.mrboomdev.safeargsnext.SafeArgsIntent
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity
import com.mrboomdev.safeargsnext.value.serializableFunction

class ExecutorActivity: Activity(), SafeArgsActivity<ExecutorActivity.Args> {
	data class Args(
		val command: ExecutorActivity.() -> Unit,
		val finishAfterExecution: Boolean = true
	)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		safeArgs.apply {
			if(this == null) {
				throw IllegalStateException("ExecutorActivity must receive at least some arguments!")
			}
			
			command()
			
			if(finishAfterExecution) {
				finish()
			}
		}
	}
}

/**
 * Function has to be serializable! Use [serializableFunction] to do so.
 */
@SerializableRequired
fun Context.executeInActivity(action: (Activity) -> Unit) {
	if(this is Activity) {
		action(this)
		return
	}
	
	startActivity(SafeArgsIntent(this, ExecutorActivity::class, ExecutorActivity.Args(action)))
}