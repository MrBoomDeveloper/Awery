package com.mrboomdev.awery.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.MainThread
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.util.UniqueIdGenerator
import org.jetbrains.annotations.Contract
import java.lang.reflect.InvocationTargetException
import java.util.Objects
import java.util.WeakHashMap
import kotlin.system.exitProcess

open class AweryLifecycle private constructor() : ActivityLifecycleCallbacks {

	@Suppress("UNCHECKED_CAST")
	private fun <A : Activity?> getActivityInfo(activity: A): ActivityInfo<A> {
		return infos.computeIfAbsent(activity) { ActivityInfo(it) } as ActivityInfo<A>
	}

	override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
		if(infos.contains(activity)) return

		infos[activity] = ActivityInfo(activity).apply {
			lastActiveTime = System.currentTimeMillis()
		}
	}

	override fun onActivityStarted(activity: Activity) {}

	override fun onActivityResumed(activity: Activity) {
		getActivityInfo(activity).apply {
			isPaused = false
			lastActiveTime = System.currentTimeMillis()
		}
	}

	override fun onActivityPaused(activity: Activity) {
		getActivityInfo(activity).isPaused = true
	}

	override fun onActivityStopped(activity: Activity) {
		getActivityInfo(activity).isPaused = true
	}

	override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

	override fun onActivityDestroyed(activity: Activity) {
		getActivityInfo(activity).isPaused = true
	}

	/**
	 * DO NOT EVER USE DIRECTLY THIS CLASS!
	 * It was made just for the Android Framework to work properly!
	 */
	internal class CallbackFragment(
		private val fragmentManager: FragmentManager,
		private val activityResultCallback: ((resultCode: Int, data: Intent?) -> Unit)? = null,
		private val permissionsResultCallback: ((didGranted: Boolean) -> Unit)? = null,
		private val requestCode: Int
	) : Fragment() {

		override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
			if(requestCode != this.requestCode || activityResultCallback == null) return
			activityResultCallback.invoke(resultCode, data)
			finish()
		}

		override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
			if(requestCode != this.requestCode || permissionsResultCallback == null) return

			if(permissions.isEmpty()) {
				permissionsResultCallback.invoke(false)
			} else if(permissions.size == 1) {
				permissionsResultCallback.invoke(grantResults[0] == PackageManager.PERMISSION_GRANTED)
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

	private class ActivityInfo<A : Activity?>(val activity: A) : Comparable<ActivityInfo<A>?> {
		var lastActiveTime: Long = 0
		var isPaused: Boolean = false

		override fun compareTo(other: ActivityInfo<A>?): Int {
			if(other == null) {
				return 1
			}

			val realMe = infos[activity]
			val realHe = infos[other.activity]

			if(realMe != null) {
				isPaused = realMe.isPaused
				lastActiveTime = realMe.lastActiveTime
			}

			if(realHe != null) {
				other.isPaused = realHe.isPaused
				other.lastActiveTime = realHe.lastActiveTime
			}

			if(lastActiveTime != 0L && other.lastActiveTime != 0L) {
				if(lastActiveTime > other.lastActiveTime) return 1
				if(lastActiveTime < other.lastActiveTime) return -1
			}

			if(isPaused && !other.isPaused) return -1
			if(!isPaused && other.isPaused) return 1

			if(activity!!.isDestroyed && !other.activity!!.isDestroyed) return -1
			if(!activity.isDestroyed && other.activity!!.isDestroyed) return 1

			if(hasWindowFocus(activity) && !hasWindowFocus(other.activity!!)) return 1
			if(!hasWindowFocus(activity) && hasWindowFocus(other.activity!!)) return -1

			return activity.taskId.compareTo(other.activity!!.taskId)
		}

		/**
		 * Sometimes Android do throw this exception "java.lang.RuntimeException: Window couldn't find content container view".
		 * Because we just need to check if the activity has focus or not we ignore the exception and return false.
		 * @return true if the activity has focus or false if it doesn't
		 * @author MrBoomDev
		 */
		private fun hasWindowFocus(activity: Activity): Boolean {
			return try {
				activity.hasWindowFocus()
			} catch(e: RuntimeException) {
				false
			}
		}
	}

	enum class Permission(val manifestConstants: String?) {
		NOTIFICATIONS(if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS else null),
		STORAGE(if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Manifest.permission.MANAGE_EXTERNAL_STORAGE else Manifest.permission.WRITE_EXTERNAL_STORAGE)
	}

	companion object {
		private val infos = WeakHashMap<Activity, ActivityInfo<Activity>>()
		private const val TAG = "AweryLifecycle"
		private var handler: Handler? = null

		private val activityRequestCodes = UniqueIdGenerator(
			1, UniqueIdGenerator.OverflowMode.RESET)

		@JvmStatic
		fun generateRequestCode(): Int {
			return activityRequestCodes.integer
		}

		@JvmStatic
		fun restartApp() {
			Log.i(TAG, "restartApp() has been invoked!")

			app!!.startActivity(Intent.makeRestartActivityTask(
				app!!.packageManager.getLaunchIntentForPackage(app!!.packageName)!!.component
			).apply {
				setPackage(app!!.packageName)
			})

			app = null
			exitProcess(0)
		}

		fun exitApp() {
			val activity = getAnyActivity(Activity::class.java)
			app = null

			if(activity != null) activity.finishAffinity()
			else Runtime.getRuntime().exit(0)
		}

		@MainThread
		private fun addActivityResultListener(
			activity: Activity,
			requestCode: Int,
			activityResultCallback: ((resultCode: Int, data: Intent?) -> Unit)?,
			permissionsResultCallback: ((didGranted: Boolean) -> Unit)?
		): Fragment {
			if(activity is FragmentActivity) {
				return activity.supportFragmentManager.let {
					val fragment = CallbackFragment(it, activityResultCallback, permissionsResultCallback, requestCode)
					it.beginTransaction().add(fragment, null).commit()
					it.executePendingTransactions()
					fragment
				}
			} else {
				throw IllegalArgumentException("Activity must be an instance of FragmentActivity!")
			}
		}

		/**
		 * This method is a little bit hacky so after library update it may break.
		 * @param context Context from the [FragmentActivity]
		 * @author MrBoomDev
		 */
		@JvmStatic
		@Suppress("deprecation")
		@MainThread
		@JvmOverloads
		fun startActivityForResult(
			context: Activity,
			intent: Intent,
			activityResultCallback: ((resultCode: Int, data: Intent?) -> Unit),
			requestCode: Int = generateRequestCode()
		) {
			addActivityResultListener(context, requestCode, activityResultCallback, null)
				.startActivityForResult(intent, requestCode)
		}

		@JvmOverloads
		fun requestPermission(
			context: Activity,
			permission: Permission,
			callback: (didGrant: Boolean) -> Unit,
			requestCode: Int = generateRequestCode()
		) {
			if(permission.manifestConstants == null ||
				ContextCompat.checkSelfPermission(context, permission.manifestConstants) == PackageManager.PERMISSION_GRANTED
			) {
				callback(true)
				return
			}

			addActivityResultListener(context, requestCode, null, callback)
			ActivityCompat.requestPermissions(context, arrayOf(permission.manifestConstants), requestCode)
		}

		@JvmStatic
		@Deprecated(message = "Java")
		fun getActivity(context: Context): Activity? {
			if(context is Activity) {
				return context
			}

			if(context is ContextWrapper) {
				return getActivity(context.baseContext)
			}

			return null
		}

		inline fun <reified A : Activity> getActivities(): List<A> {
			return getActivities(A::class.java)
		}

		@JvmStatic
		fun <A : Activity> getActivities(requiredSuper: Class<A>): List<A> {
			try {
				return getAllActivitiesRecursively(requiredSuper)
					.sortedDescending()
					.map { it.activity }
					.toList()
			} catch(e: Exception) {
				Log.e(TAG, "Failed to get activities!", e)
				toast("Your device is not supported :(", 1)
				exitProcess(0)
			}
		}

		inline fun <reified A : Activity> getAnyActivity(): A? {
			return getAnyActivity(A::class.java)
		}

		@JvmStatic
		@Contract(pure = true)
		fun <A : Activity> getAnyActivity(requiredSuper: Class<A>): A? {
			try {
				return getAllActivitiesRecursively(requiredSuper).apply {
					if(size == 1) {
						return this[0].activity
					}

					// App should handle this behaviour properly or else crashes will occur
					if(isEmpty()) return null
				}.sortedDescending()[0].activity
			} catch(e: Exception) {
				Log.e(TAG, "Failed to get any activity!", e)
				toast("So your device is not supported :(", 1)
				exitProcess(0)
			}
		}

		@Suppress("UNCHECKED_CAST")
		@Throws(NoSuchFieldException::class, IllegalAccessException::class)
		private fun <A : Activity?> getAllActivitiesRecursively(requiredSuper: Class<A>): List<ActivityInfo<A>> {
			val list = ArrayList<ActivityInfo<A>>()

			val activityThread = activityThread ?: return list

			val activities = activityThread.javaClass.getDeclaredField("mActivities").let {
				it.isAccessible = true
				it.get(activityThread)
			}

			if(activities is Map<*, *>) {
				for(record in activities.values) {
					if(record == null) continue

					val recordClass: Class<*> = record.javaClass
					val activityField = recordClass.getDeclaredField("activity")
					activityField.isAccessible = true

					val activity = activityField[record] as Activity

					if(!requiredSuper.isInstance(activity)) {
						continue
					}

					val info = ActivityInfo(activity as A)
					list.add(info)

					val pausedField = recordClass.getDeclaredField("paused")
					pausedField.isAccessible = true
					info.isPaused = Objects.requireNonNullElse(pausedField[record] as Boolean, false)

					list.add(info)
				}
			}

			return list
		}

		@get:SuppressLint("DiscouragedPrivateApi", "PrivateApi")
		private val activityThread: Any?
			get() {
				try {
					val clazz = Class.forName("android.app.ActivityThread")
					val field = clazz.getDeclaredField("sCurrentActivityThread")
					field.isAccessible = true
					val value = field[null]
					if(value != null) return value
				} catch(ignored: Exception) {
				}

				try {
					val clazz = Class.forName("android.app.AppGlobals")
					val field = clazz.getDeclaredField("sCurrentActivityThread")
					field.isAccessible = true
					val value = field[null]
					if(value != null) return value
				} catch(ignored: Exception) {
				}

				try {
					val clazz = Class.forName("android.app.ActivityThread")
					val method = clazz.getDeclaredMethod("currentActivityThread")
					method.isAccessible = true
					return method.invoke(null)
				} catch(ignored: Exception) {
					return null
				}
			}

		@JvmStatic
		fun postRunnable(runnable: Runnable?): Runnable? {
			return if(handler!!.post(runnable!!)) runnable else null
		}

		@JvmStatic
		fun runOnUiThread(runnable: Runnable): Runnable {
			if(!isMainThread) handler!!.post(runnable)
			else runnable.run()

			return runnable
		}

		private val isMainThread: Boolean
			get() = Looper.getMainLooper() == Looper.myLooper()

		/**
		 * Runs the callback on the main thread and checks whatever RecyclerView is computing layout or not to avoid exceptions.
		 * @param callback Will be ran on the main thread if RecyclerView isn't computing layout
		 * @param recycler Target RecyclerView
		 * @return May be a different callback depending on the state of the RecyclerView, so that you can cancel it.
		 */
		@JvmStatic
		fun runOnUiThread(callback: Runnable, recycler: RecyclerView): Runnable {
			if(!isMainThread || recycler.isComputingLayout) {
				val runnable = Runnable { runOnUiThread(callback, recycler) }
				handler!!.post(runnable)
				return runnable
			}

			callback.run()
			return callback
		}

		@JvmStatic
		@Deprecated(message = "old shit for java")
		fun getContext(binding: ViewBinding?): Context? {
			if(binding == null) return null
			return binding.root.context
		}

		@JvmStatic
		@Contract("null -> null")
		@Deprecated(message = "old shit for java")
		fun getContext(view: View?): Context? {
			if(view == null) return null
			return view.context
		}

		@JvmStatic
		@Deprecated(message = "old shit for java")
		fun getContext(inflater: LayoutInflater): Context {
			return inflater.context
		}

		@JvmStatic
		val anyContext: Context
			get() {
				val activity: Activity?

				try {
					activity = getAnyActivity(Activity::class.java)
					if(activity != null) return activity
				} catch(ignored: IndexOutOfBoundsException) { }

				return appContext
			}

		@get:SuppressLint("PrivateApi", "DiscouragedPrivateApi")
		private val contextUsingPrivateApi: App?
			get() {
				var context = invokeMethod(
					"android.app.ActivityThread",
					"currentApplication"
				) as App?

				if(context != null) {
					return context
				}

				context = invokeMethod(
					"android.app.AppGlobals",
					"getInitialApplication"
				) as App?

				return context
			}

		private fun invokeMethod(className: String, methodName: String): Any? {
			try {
				val clazz = Class.forName(className)
				val method = clazz.getMethod(methodName)
				method.isAccessible = true
				return method.invoke(null)
			} catch(e: ClassNotFoundException) {
				return null
			} catch(e: IllegalAccessException) {
				return null
			} catch(e: InvocationTargetException) {
				return null
			} catch(e: NoSuchMethodException) {
				return null
			}
		}

		private var app: App? = null

		@JvmStatic
		var appContext: App
			get() {
				contextUsingPrivateApi.let {
					if(it != null) {
						app = it
						return it
					}
				}

				getAnyActivity(Activity::class.java)?.let {
					app = it.applicationContext as App
					return app!!
				}

				return app!!
			}

			set(value) {
				app = value
				value.registerActivityLifecycleCallbacks(AweryLifecycle())
				handler = Handler(Looper.getMainLooper())
			}

		@JvmStatic
		fun cancelDelayed(runnable: Runnable?) {
			handler!!.removeCallbacks(runnable!!)
		}

		@JvmStatic
		fun runDelayed(runnable: Runnable?, delay: Long) {
			handler!!.postDelayed(runnable!!, delay)
		}

		@JvmStatic
		fun runDelayed(runnable: Runnable, delay: Long, recycler: RecyclerView): Runnable {
			val result = Runnable { runOnUiThread(runnable, recycler) }
			handler!!.postDelayed(result, delay)
			return result
		}
	}
}