package com.mrboomdev.awery.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

// TODO: Load feeds from the home page in background
// so that user would have fresh content every day
// without waiting for all of the load in the app.
class UpdateFeedWorker(
	context: Context,
	params: WorkerParameters
): CoroutineWorker(context, params) {
	override suspend fun doWork(): Result {
		TODO("Not yet implemented")
	}
}