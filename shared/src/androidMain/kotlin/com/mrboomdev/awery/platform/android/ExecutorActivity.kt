package com.mrboomdev.awery.platform.android

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.mrboomdev.safeargsnext.SafeArgsIntent
import com.mrboomdev.safeargsnext.owner.SafeArgsActivity

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

fun Context.execute(command: ExecutorActivity.() -> Unit) {
	startActivity(SafeArgsIntent(this, ExecutorActivity::class, ExecutorActivity.Args(command)))
}