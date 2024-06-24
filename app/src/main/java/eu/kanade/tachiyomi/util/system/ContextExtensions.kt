package eu.kanade.tachiyomi.util.system

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import androidx.core.content.PermissionChecker
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.mrboomdev.awery.app.AweryApp.toast

/**
 * Checks if the give permission is granted.
 *
 * @param permission the permission to check.
 * @return true if it has permissions.
 */
fun Context.hasPermission(permission: String) = PermissionChecker.checkSelfPermission(this, permission) == PermissionChecker.PERMISSION_GRANTED

val Context.powerManager: PowerManager
    get() = getSystemService()!!

/**
 * Convenience method to acquire a partial wake lock.
 */
fun Context.acquireWakeLock(tag: String): PowerManager.WakeLock {
    val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$tag:WakeLock")
    wakeLock.acquire()
    return wakeLock
}

/**
 * Returns true if the given service class is running.
 */
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val className = serviceClass.name
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION")
    return manager.getRunningServices(Integer.MAX_VALUE)
        .any { className == it.service.className }
}

fun Context.openInBrowser(url: String, forceDefaultBrowser: Boolean = false) {
    this.openInBrowser(url.toUri(), forceDefaultBrowser)
}

fun Context.openInBrowser(uri: Uri, forceDefaultBrowser: Boolean = false) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            // Force default browser so that verified extensions don't re-open Tachiyomi
            if (forceDefaultBrowser) {
                defaultBrowserPackageName()?.let { setPackage(it) }
            }
        }
        startActivity(intent)
    } catch(e: Exception) {
        toast(e.message)
    }
}

private fun Context.defaultBrowserPackageName(): String? {
    val browserIntent = Intent(Intent.ACTION_VIEW, "http://".toUri())
    val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.resolveActivity(browserIntent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
    } else {
        packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY)
    }

    return resolveInfo
        ?.activityInfo?.packageName
        ?.takeUnless { it in DeviceUtil.invalidDefaultBrowsers }
}

/**
 * Returns true if [packageName] is installed.
 */
fun Context.isPackageInstalled(packageName: String): Boolean {
    return try {
        packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

val Context.hasMiuiPackageInstaller get() = isPackageInstalled("com.miui.packageinstaller")

val Context.isShizukuInstalled get() = false