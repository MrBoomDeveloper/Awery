package com.mrboomdev.awery.app

import android.app.NotificationManager
import android.os.Build
import androidx.compose.runtime.Composer
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.core.content.getSystemService
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mrboomdev.awery.app.workers.UpdateLibraryWorker
import com.mrboomdev.awery.core.Awery
import com.mrboomdev.awery.core.context
import com.mrboomdev.awery.core.utils.Log
import com.mrboomdev.awery.data.isDebug
import com.mrboomdev.awery.data.notifications.AweryNotification
import java.util.concurrent.TimeUnit

internal actual fun platformInit() {
    if(Awery.isDebug) {
        Log.i("AweryInit", "Launched in debug mode")
        
        @OptIn(ExperimentalComposeRuntimeApi::class)
        Composer.setDiagnosticStackTraceEnabled(true)
    }
    
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { 
        Awery.context.getSystemService<NotificationManager>()!!.apply {
            for(channel in AweryNotification.Channel.entries) {
				createNotificationChannel(channel.create())
			}
		}
    }
    
    WorkManager.getInstance(Awery.context)
        .enqueueUniquePeriodicWork(
            uniqueWorkName = "updateLibrary",
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = PeriodicWorkRequestBuilder<UpdateLibraryWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            ).apply {
                setConstraints(Constraints(
                    requiredNetworkType = NetworkType.CONNECTED,
                    requiresBatteryNotLow = true
                ))
            }.build()
        )
}