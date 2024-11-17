package com.mrboomdev.awery.sources.yomi

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.mrboomdev.awery.app.App.Companion.toast
import com.mrboomdev.awery.app.AweryLifecycle.Companion.generateRequestCode
import com.mrboomdev.awery.app.ExtensionsManager.isEnabled
import com.mrboomdev.awery.ext.source.DataSource
import com.mrboomdev.awery.ext.source.SourcesManager
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.util.UniqueIdGenerator
import dalvik.system.PathClassLoader
import eu.kanade.tachiyomi.network.NetworkHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.addSingleton
import uy.kohesive.injekt.api.addSingletonFactory
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.concurrent.CancellationException

abstract class YomiManager<S, T : YomiSource>(
	val context: Context
): SourcesManager<T>() {
	private val pendingIntentIds = UniqueIdGenerator(Long.MIN_VALUE, UniqueIdGenerator.OverflowMode.RESET)
	private val sources = HashMap<String, T>()
	abstract val minVersion: Double
	abstract val maxVersion: Double
	abstract val appLabelPrefix: String
	abstract val mainClass: String
	abstract val requiredFeature: String
	abstract val nsfwMeta: String

	/**
	 * It is marked as nullable, to be able to recalculate it's values,
	 * but the returned value is always non-null.
	 */
	private var sourcesList: List<T>? = null
		get() {
			var it = field

			if(field == null) {
				it = sources.values.toList()
				field = it
			}

			return it
		}

	override fun get(id: String): T {
		return sources[id] ?: throw NoSuchElementException(id)
	}

	override fun getAll(): List<T> {
		return sourcesList!!
	}

	override suspend fun load(id: String) {
		createSource(id, true)
	}

	override suspend fun unload(id: String) {
		createSource(id, false)
	}

	private fun getPackages(context: Context): List<PackageInfo> {
		return context.packageManager.getInstalledPackages(PM_FLAGS)
			.filter { info ->
				if(info.reqFeatures == null) return@filter false

				for(feature in info.reqFeatures!!) {
					if(feature.name == null) continue
					if(feature.name == requiredFeature) return@filter true
				}

				false
			}.toList()
	}

	override fun loadAll() = channelFlow {
		val packages = getPackages(context)

		val progress = Progress(packages.size.toLong())
		send(progress)

		coroutineScope {
			getPackages(context).map { pkg ->
				async {
					createSource(pkg.packageName, isEnabled(pkg.packageName))
					progress.increment()
					send(progress)
				}
			}.awaitAll()

			progress.isCompleted = true
			send(progress)
		}
	}

	override fun unloadAll() = flow {
		val progress = Progress(sources.size.toLong())
		emit(progress)

		sources.clear()
		progress.isCompleted = true
		emit(progress)
	}

	abstract fun getSourceLongId(source: S): Long

	private fun createSource(packageName: String, init: Boolean): T {
		var throwable: Throwable? = null

		val packageInfo = context.packageManager.getPackageInfo(packageName,
			PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA)

		val label = packageInfo.applicationInfo!!.loadLabel(context.packageManager).let { appLabel ->
			if(appLabel.startsWith(appLabelPrefix)) {
				return@let appLabel.substring(appLabelPrefix.length).trim { it <= ' ' }
			} else appLabel
		}.toString()

		val isNsfw = packageInfo.applicationInfo!!.metaData.getInt(nsfwMeta, 0) == 1

		val sources = if(init) {
			try {
				checkSupportedVersionBounds(packageInfo.versionName!!)

				try {
					instantiateMains(packageInfo)
				} catch(e: Throwable) {
					throwable = e
					null
				}
			} catch(e: UnsupportedOperationException) {
				throwable = e
				null
			}

		} else null

		return createSourceWrapper(label, isNsfw, packageInfo, sources, throwable)
	}

	private fun instantiateMains(packageInfo: PackageInfo): Array<Any> {
		val classLoader = PathClassLoader(
			packageInfo.applicationInfo!!.sourceDir,
			null,
			context.classLoader)

		val mainClassesString = packageInfo.applicationInfo!!.metaData.getString(mainClass)
			?: throw NullPointerException("Main classes not found!")

		return mainClassesString.split(";").dropLastWhile { it.isEmpty() }.toTypedArray().map { fullClassName ->
			var mainClass = fullClassName

			if(mainClass.startsWith(".")) {
				mainClass = packageInfo.packageName + mainClass
			}

			return@map Class.forName(mainClass, false, classLoader).getConstructor().let {
				it.isAccessible = true
				it.newInstance()
			}
		}.filterNotNull().toTypedArray()
	}

	@Throws(UnsupportedOperationException::class)
	private fun checkSupportedVersionBounds(versionName: String) {
		versionName.let {
			val secondDotIndex = versionName.indexOf(".", versionName.indexOf(".") + 1)

			if(secondDotIndex != -1) {
				versionName.substring(0, secondDotIndex)
			} else it
		}.toDouble().let { version ->
			if(version < minVersion) throw UnsupportedOperationException("Extension is made for an older deprecated version.")
			if(version > maxVersion) throw UnsupportedOperationException("Extension is made for an unsupported version.")
		}
	}

	@Throws(IllegalArgumentException::class)
	abstract fun createSourceWrapper(
		label: String,
		isNsfw: Boolean,
		packageInfo: PackageInfo,
		sources: Array<Any>?,
		exception: Throwable?
	): T

	@Throws(ExtensionInstallException::class, CancellationException::class)
	override suspend fun install(data: DataSource<InputStream>): T {
		val pi = context.packageManager.packageInstaller
		val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
			setSize(data.size ?: -1)

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				setInstallerPackageName(context.packageName)
				setRequestUpdateOwnership(true)
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				setPackageSource(PackageInstaller.PACKAGE_SOURCE_STORE)
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
			}

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				setInstallReason(PackageManager.INSTALL_REASON_USER)
			}
		}

		val sessionId = pi.createSession(params)
		val session = pi.openSession(sessionId)

		session.openWrite("package", 0, data.size ?: -1).use { output ->
			BufferedInputStream(data.data).use { input ->
				val buffer = ByteArray(16484)
				var n: Int

				while(input.read(buffer).apply { n = this } >= 0) {
					output.write(buffer, 0, n)
				}
			}
		}

		val operationId = pendingIntentIds.long
		var result: Any? = null

		val intentSender = PendingIntent.getBroadcast(context, generateRequestCode(),
			Intent(context, PackageManagerReceiver::class.java).apply {
				putExtra(PackageManagerReceiver.ID_KEY, operationId)
			}, PendingIntent.FLAG_IMMUTABLE).intentSender

		PackageManagerReceiver.addListener(operationId) {
			val message = it.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

			when(it.getIntExtra(PackageInstaller.EXTRA_STATUS, 0)) {
				PackageInstaller.STATUS_SUCCESS -> {
					toast("Installed!", 1)
					result = it
				}

				PackageInstaller.STATUS_PENDING_USER_ACTION -> {
					toast("Pending user action...", 1)
				}

				PackageInstaller.STATUS_FAILURE_BLOCKED -> result = ExtensionInstallException(
					"The install has been blocked by your device!", IllegalStateException(message))

				PackageInstaller.STATUS_FAILURE_CONFLICT -> result = ExtensionInstallException(
					"An conflict has occurred while trying to install!", IllegalStateException(message))

				PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> result = ExtensionInstallException(
					"Sorry, but your device isn't compatible with this extension!", IllegalStateException(message))

				PackageInstaller.STATUS_FAILURE_INVALID -> result = ExtensionInstallException(
					"Extension file appears to be invalid!", IllegalStateException(message))

				PackageInstaller.STATUS_FAILURE_STORAGE -> result = ExtensionInstallException(
					"You don't have enough space!", IllegalStateException(message))

				PackageInstaller.STATUS_FAILURE_TIMEOUT -> result = ExtensionInstallException(
					"Your device just got tired and cancelled an install!", IllegalStateException(message))

				PackageInstaller.STATUS_FAILURE_ABORTED -> result = CancellationException()
				else -> result = ExtensionInstallException("Failed to install an extension!", IllegalStateException(message))
			}
		}

		session.commit(intentSender)

		while(result == null) {
			delay(100)
		}

		PackageManagerReceiver.removeListener(operationId)

		if(result is Throwable) {
			throw result as Throwable
		}

		(result as? Intent)?.let {
			return createSource(it.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)!!, true)
		}

		throw IllegalStateException("Unknown type of result!")
	}

	override suspend fun uninstall(id: String) {
		val pi = context.packageManager.packageInstaller
		val operationId = pendingIntentIds.long
		var result: Any? = null

		val intentSender = PendingIntent.getBroadcast(context, generateRequestCode(),
			Intent(context, PackageManagerReceiver::class.java).apply {
				putExtra(PackageManagerReceiver.ID_KEY, operationId)
			}, PendingIntent.FLAG_IMMUTABLE).intentSender

		PackageManagerReceiver.addListener(operationId) {
			when(it.getIntExtra(PackageInstaller.EXTRA_STATUS, 0)) {
				PackageInstaller.STATUS_SUCCESS -> {
					toast("Uninstalled!", 1)
					result = true
				}

				PackageInstaller.STATUS_PENDING_USER_ACTION -> {
					toast("Pending user action...", 1)
				}

				PackageInstaller.STATUS_FAILURE_BLOCKED -> result = IllegalStateException(
					"The uninstall has been blocked by your device!")

				PackageInstaller.STATUS_FAILURE_CONFLICT -> result = IllegalStateException(
					"An conflict has occurred while trying to uninstall!")

				PackageInstaller.STATUS_FAILURE_TIMEOUT -> result = IllegalStateException(
					"Your device just got tired and cancelled an uninstall!")

				PackageInstaller.STATUS_FAILURE_ABORTED -> result = CancellationException()
				else -> result = IllegalStateException("Failed to uninstall an extension!")
			}
		}

		pi.uninstall(id, intentSender)

		while(result == null) {
			delay(100)
		}

		PackageManagerReceiver.removeListener(operationId)

		if(result is Throwable) {
			throw result as Throwable
		}

		if(result == true) return
		throw IllegalStateException("Unknown type of result!")
	}

	companion object {
		private const val PM_FLAGS = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA

		fun initYomiShit(context: Context) {
			Injekt.addSingleton<Application>(context.applicationContext as Application)
			Injekt.addSingleton(NetworkHelper(context))

			Injekt.addSingletonFactory {
				Json {
					ignoreUnknownKeys = true
					explicitNulls = false
				}
			}
		}
	}

	class PackageManagerReceiver: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			if(intent == null || !intent.hasExtra(ID_KEY)) return

			val key = intent.getLongExtra(ID_KEY, 0)
			val listener = listeners[key]

			if(listener == null) {
				Log.e(TAG, "No listener found with such id!")
				return
			}

			listener(intent)
		}

		companion object {
			private val listeners = HashMap<Long, (intent: Intent) -> Unit>()
			private const val TAG = "PackageManagerReceiver"
			const val ID_KEY = "ID"

			fun addListener(id: Long, listener: (intent: Intent) -> Unit) {
				listeners[id] = listener
			}

			fun removeListener(id: Long) {
				listeners.remove(id)
			}
		}
	}
}