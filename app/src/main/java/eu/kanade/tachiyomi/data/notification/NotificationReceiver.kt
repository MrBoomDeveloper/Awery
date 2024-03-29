package eu.kanade.tachiyomi.data.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver {

    companion object {
        private const val NAME = "NotificationReceiver"


        /**
         * Returns [PendingIntent] that opens the extensions controller.
         *
         * @param context context of application
         * @return [PendingIntent]
         */
        internal fun openExtensionsPendingActivity(): PendingIntent? {
            return null
        }
    }
}