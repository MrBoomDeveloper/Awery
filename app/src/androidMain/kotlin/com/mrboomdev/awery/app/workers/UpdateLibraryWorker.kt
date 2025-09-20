package com.mrboomdev.awery.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.mrboomdev.awery.data.notifications.AweryNotification
import kotlinx.coroutines.delay

class UpdateLibraryWorker(
	private val context: Context,
	params: WorkerParameters
): CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		// TODO: Actually update library and add notifications
		// to the notifications tab and also publish them to the
		// notifications sheet
		println("Hello from background!")
		setForeground(getForegroundInfo())

		delay(10_000)

//		AweryNotification(
//			title = "Test notification from Awery! 📦",
//			channel = AweryNotification.Channel.OTHER
//		).publish()
		
		return Result.success()
	}

	override suspend fun getForegroundInfo(): ForegroundInfo {
		println("get foreground")
		val notification = AweryNotification.UpdatingLibrary
		
		return ForegroundInfo(
			notification.id,
			notification.create(context)
		)
	}
}