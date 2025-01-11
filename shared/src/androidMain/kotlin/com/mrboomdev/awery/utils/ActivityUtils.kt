package com.mrboomdev.awery.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.annotation.MainThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.util.WeakHashMap

private val backPressedCallbacks = WeakHashMap<() -> Unit, Any>()

fun Activity.removeOnBackPressedListener(callback: () -> Unit) {
    val onBackInvokedCallback = backPressedCallbacks.remove(callback) ?: return

    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        onBackInvokedDispatcher.unregisterOnBackInvokedCallback(callback)
    } else {
        (onBackInvokedCallback as OnBackPressedCallback).remove()
    }
}

fun Activity.addOnBackPressedListener(callback: () -> Unit) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        backPressedCallbacks[callback] = callback
        onBackInvokedDispatcher.registerOnBackInvokedCallback(0, callback)
    } else {
        if(this is OnBackPressedDispatcherOwner) {
            val onBackInvokedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    callback()
                }
            }

            onBackPressedDispatcher.addCallback(this, onBackInvokedCallback)
            backPressedCallbacks[callback] = onBackInvokedCallback
        } else {
            throw IllegalArgumentException("Activity must implement OnBackPressedDispatcherOwner!")
        }
    }
}

private val activityRequestCodes = UniqueIdGenerator(
    1, UniqueIdGenerator.OverflowMode.RESET)

fun generateRequestCode(): Int {
    return activityRequestCodes.integer
}

@MainThread
fun Activity.addActivityResultListener(
    requestCode: Int,
    activityResultCallback: ((resultCode: Int, data: Intent?) -> Unit)?,
    permissionsResultCallback: ((didGranted: Boolean) -> Unit)?
): Fragment {
    if(this is FragmentActivity) {
        return supportFragmentManager.let { fragmentManager ->
            val fragment = CallbackFragment().apply {
                this.fragmentManager = fragmentManager
                this.requestCode = requestCode
                this.activityResultCallback = activityResultCallback
                this.permissionsResultCallback = permissionsResultCallback
            }

            fragmentManager.beginTransaction().add(fragment, null).commit()
            fragmentManager.executePendingTransactions()
            return@let fragment
        }
    } else {
        throw UnsupportedOperationException("Activity must be an instance of FragmentActivity!")
    }
}

@MainThread
fun Activity.requestPermission(
    permission: String,
    callback: (didGrant: Boolean) -> Unit,
    requestCode: Int = generateRequestCode()
) {
    if(hasPermission(permission)) {
        callback(true)
        return
    }

    addActivityResultListener(requestCode, null, callback)
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}

/**
 * This method is a little bit hacky so after library update it may break.
 */
@Suppress("deprecation")
@JvmOverloads
@MainThread
fun Activity.startActivityForResult(
    intent: Intent,
    activityResultCallback: ((resultCode: Int, data: Intent?) -> Unit),
    requestCode: Int = generateRequestCode()
) {
    addActivityResultListener(requestCode, activityResultCallback, null)
        .startActivityForResult(intent, requestCode)
}

/**
 * DO NOT EVER USE DIRECTLY THIS CLASS!
 * It was made just for the Android Framework to work properly!
 */
@Suppress("OVERRIDE_DEPRECATION")
internal class CallbackFragment : Fragment() {
    internal lateinit var fragmentManager: FragmentManager
    internal var requestCode: Int = 0
    internal var activityResultCallback: ((Int, Intent?) -> Unit)? = null
    internal var permissionsResultCallback: ((didGranted: Boolean) -> Unit)? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode != this.requestCode) return
        activityResultCallback?.invoke(resultCode, data)
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if(requestCode != this.requestCode) return

        if(permissions.isEmpty()) {
            permissionsResultCallback?.invoke(false)
        } else if(permissions.size == 1) {
            permissionsResultCallback?.invoke(grantResults[0] == PackageManager.PERMISSION_GRANTED)
        } else {
            throw IllegalStateException("Somehow you've requested multiple permissions at once. This behaviour isn't supported.")
        }

        finish()
    }

    private fun finish() {
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.executePendingTransactions()
    }
}