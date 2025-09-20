package com.mrboomdev.awery.data.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.mrboomdev.awery.core.utils.AppPermission
import com.mrboomdev.awery.core.utils.IntPool
import com.mrboomdev.awery.core.utils.hasPermission

private val idPool = IntPool(1..Int.MAX_VALUE)

class AweryNotification(
	val id: Int = idPool.next(),
	val channel: Channel = Channel.OTHER,
	var title: String,
	var progress: Progress? = null,
	val category: String? = null
) {
	fun publish() {
		if(!Awery.hasPermission(AppPermission.NOTIFICATION)) return
		val manager = Awery.context.getSystemService(NotificationManager::class.java)
		manager.notify(id, create(Awery.context))
	}
	
	fun create(context: Context): Notification {
		return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Notification.Builder(context, channel.id)
		} else {
			@Suppress("DEPRECATION")
			Notification.Builder(context)
		}.apply {
			// TODO: Create a separate outlined icon 
			//  (Impl note: Place drawable in the app module and 
			//  access it by using reflection. That's the only way)
			setSmallIcon(Awery.context.applicationInfo.icon)
			setContentTitle(title)
			setCategory(category)
			
			progress?.also {
				setProgress(it.max, it.current, it.indeterminate)
			}
		}.build()
	}
	
	class Progress private constructor(
		val current: Int,
		val max: Int,
		val indeterminate: Boolean
	) {
		constructor(
			current: Int,
			max: Int
		): this(current, max, false)
		
		companion object {
			val Indeterminate = Progress(
				current = 0,
				max = 0,
				indeterminate = true
			)
		}
	}
	
	enum class Channel(
		private val title: String,
		private val importance: Int
	) {
		CHECKING_UPDATES(
			title = "Checking for updates",
			importance = NotificationManager.IMPORTANCE_MIN
		),
		
		OTHER(
			title = "Other",
			importance = NotificationManager.IMPORTANCE_DEFAULT
		);
		
		val id = name
		
		@RequiresApi(Build.VERSION_CODES.O)
		fun create(): NotificationChannel {
			return NotificationChannel(
				name,
				title,
				importance
			)
		}
	}
	
	companion object {
		val UpdatingLibrary = AweryNotification(
			id = 0,
			title = "Updating library",
			progress = Progress.Indeterminate,
			channel = Channel.CHECKING_UPDATES,
			category = Notification.CATEGORY_PROGRESS
		)
	}
}