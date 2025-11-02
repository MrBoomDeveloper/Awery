package com.mrboomdev.awery.core.utils

import android.content.pm.PackageManager
import android.os.Build
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context

/**
 * Checks if the application has the specified permission.
 * @param permission The permission to check for.
 * @return True if the application has the permission, false otherwise.
 */
fun Awery.hasPermission(permission: AppPermission): Boolean {
	if(permission.constant == null) return true
	
	return context.checkSelfPermission(
		permission.constant
	) == PackageManager.PERMISSION_GRANTED
}

enum class AppPermission {
	NOTIFICATION {
		override val constant: String?
			get() = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				android.Manifest.permission.POST_NOTIFICATIONS
			} else null
	};
	
	internal abstract val constant: String?
}