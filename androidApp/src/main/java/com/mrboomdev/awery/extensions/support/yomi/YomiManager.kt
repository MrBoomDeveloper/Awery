@file:OptIn(ExperimentalStdlibApi::class)

package com.mrboomdev.awery.extensions.support.yomi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.mrboomdev.awery.app.App.Companion.getMoshi
import com.mrboomdev.awery.app.AweryLifecycle.Companion.anyContext
import com.mrboomdev.awery.data.settings.NicePreferences
import com.mrboomdev.awery.extensions.Extension
import com.mrboomdev.awery.extensions.ExtensionProvider
import com.mrboomdev.awery.extensions.ExtensionSettings
import com.mrboomdev.awery.extensions.ExtensionsManager
import com.mrboomdev.awery.data.ContentType
import com.mrboomdev.awery.util.NiceUtils
import com.mrboomdev.awery.ext.util.Progress
import com.mrboomdev.awery.ext.util.exceptions.ExtensionInstallException
import com.mrboomdev.awery.util.async.AsyncFuture
import com.mrboomdev.awery.util.async.AsyncUtils
import com.mrboomdev.awery.util.async.ControllableAsyncFuture
import com.mrboomdev.awery.util.io.HttpClient.fetchSync
import com.mrboomdev.awery.util.io.HttpRequest
import com.mrboomdev.awery.utils.activity
import com.mrboomdev.awery.utils.buildIntent
import com.mrboomdev.awery.utils.getPackageUri
import com.mrboomdev.awery.utils.startActivityForResult
import com.squareup.moshi.adapter
import dalvik.system.PathClassLoader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicReference

private const val PM_FLAGS = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA
private const val TAG = "YomiManager"

abstract class YomiManager : ExtensionsManager() {
	private val extensions: MutableMap<String, Extension> = HashMap()
	private var progress: Progress? = null
	abstract val mainClassMeta: String?
	abstract val nsfwMeta: String?
	abstract val requiredFeature: String
	abstract val prefix: String
	abstract val minVersion: Double
	abstract val maxVersion: Double
	abstract val baseFeatures: Set<String?>?

	abstract fun createProviders(extension: Extension?, main: Any?): List<ExtensionProvider?>

	override fun getExtension(id: String) = extensions[id]

	override fun getAllExtensions() = extensions.values

	override fun getProgress(): Progress {
		if(progress == null) {
			return Progress(getPackages(anyContext).size.toLong()).also { progress = it }
		}

		return progress!!
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

	override fun loadAllExtensions(context: Context) {
		for(pkg in getPackages(context)) {
			initExtension(pkg, context)
		}

		getProgress().finish()
	}

	private fun initExtension(pkg: PackageInfo, context: Context) {
		val pm = context.packageManager
		var label = pkg.applicationInfo!!.loadLabel(pm).toString()

		if(label.startsWith(prefix)) {
			label = label.substring(prefix.length).trim { it <= ' ' }
		}

		val isNsfw = pkg.applicationInfo!!.metaData.getInt(nsfwMeta, 0) == 1

		val extension = object : Extension(this, pkg.packageName, label, pkg.versionName) {
			override fun getIcon(): Drawable {
				return pkg.applicationInfo!!.loadIcon(pm)
			}
		}

		if(isNsfw) {
			extension.addFlags(Extension.FLAG_NSFW)
		}

		extensions[pkg.packageName] = extension

		try {
			checkSupportedVersionBounds(pkg.versionName!!, minVersion, maxVersion)
		} catch(e: IllegalArgumentException) {
			extension.setError("Unsupported version!", e)
			return
		}

		loadExtension(context, pkg.packageName)
	}

	override fun loadExtension(context: Context, id: String) {
		unloadExtension(context, id)

		val mains: List<*>
		val extension = extensions[id] ?: throw NullPointerException("Extension $id not found!")

		val key = ExtensionSettings.getExtensionKey(extension) + "_enabled"
		if(!NicePreferences.getPrefs().getBoolean(key, true)) {
			return
		}

		try {
			mains = loadMains(context, extension)
		} catch(t: Throwable) {
			Log.e(TAG, "Failed to load main classes!", t)
			extension.setError("Failed to load main classes!", t)
			return
		}

		val providers = mains
			.map { main -> createProviders(extension, main) }
			.flatten().toList()

		for(provider in providers) {
			extension.addProvider(provider)
		}

		extension.setIsLoaded(true)
		getProgress().increment()
	}

	override fun unloadExtension(context: Context, id: String) {
		val extension = extensions[id] ?: throw NullPointerException("Extension $id not found!")

		if(!extension.isLoaded) return

		extension.setIsLoaded(false)
		extension.clearProviders()
		extension.removeFlags(Extension.FLAG_ERROR or Extension.FLAG_WORKING)
	}

	@Throws(PackageManager.NameNotFoundException::class, ClassNotFoundException::class)
	fun loadMains(
		context: Context,
		extension: Extension
	): List<*> {
		return loadClasses(context, extension).map { clazz ->
			val constructor = clazz!!.getConstructor()
			return@map constructor.newInstance()
		}.toList()
	}

	@Throws(PackageManager.NameNotFoundException::class, ClassNotFoundException::class, NullPointerException::class)
	fun loadClasses(
		context: Context,
		extension: Extension
	): List<Class<*>?> {
		val exception = AtomicReference<Exception?>()
		val pkgInfo = context.packageManager.getPackageInfo(extension.id, PM_FLAGS)

		val classLoader = PathClassLoader(
			pkgInfo.applicationInfo!!.sourceDir,
			null,
			context.classLoader
		)

		val mainClassesString = pkgInfo.applicationInfo!!.metaData.getString(mainClassMeta)
			?: throw NullPointerException("Main classes not found!")

		val classes = mainClassesString.split(";").dropLastWhile { it.isEmpty() }.toTypedArray().map {
			var mainClass = it

			if(mainClass.startsWith(".")) {
				mainClass = pkgInfo.packageName + mainClass
			}
			try {
				return@map Class.forName(mainClass, false, classLoader)
			} catch(e: ClassNotFoundException) {
				exception.set(e)
				return@map null
			}
		}.filterNotNull().toList()

		if(exception.get() != null) {
			throw exception.get()!!
		}

		return classes
	}

	@Throws(IllegalArgumentException::class)
	fun checkSupportedVersionBounds(
		versionName: String,
		minVersion: Double,
		maxVersion: Double
	) {
		var versionName = versionName
		val secondDotIndex = versionName.indexOf(".", versionName.indexOf(".") + 1)

		if(secondDotIndex != -1) {
			versionName = versionName.substring(0, secondDotIndex)
		}

		val version = versionName.toDouble()

		require(!(version < minVersion)) { "Unsupported deprecated version!" }
		require(!(version > maxVersion)) { "Unsupported new version!" }
	}

	override fun getRepository(url: String): AsyncFuture<List<Extension>> {
		return AsyncUtils.thread<List<Extension>> {
			val response = fetchSync(HttpRequest(url))

			getMoshi().adapter<List<YomiRepoItem>>().fromJson(response.text)!!
				.map { it.toExtension(this@YomiManager, url) }
				.toList()
		}
	}

	private fun installApk(context: Context, uri: Uri, future: ControllableAsyncFuture<Extension>) = with(context.activity) {
		val tempFile = NiceUtils.getTempFile()

		try {
			context.contentResolver.openInputStream(uri).use { inputStream ->
				FileOutputStream(tempFile).use { os ->
					val buffer = ByteArray(1024 * 5)
					var read: Int
					
					while((inputStream!!.read(buffer).also { read = it }) != -1) {
						os.write(buffer, 0, read)
					}
				}
			}

			val info = packageManager.getPackageArchiveInfo(tempFile.path, PM_FLAGS) ?: run {
				future.fail(ExtensionInstallException("Failed to parse an APK!"))
				return@with
			}

			runOnUiThread {
				startActivityForResult(buildIntent(
					action = Intent.ACTION_VIEW,
					data = uri,
					type = ContentType.APK.mimeType
				) {
					putExtra(Intent.EXTRA_RETURN_RESULT, true)
					putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
					putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, packageName)
					addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				}, { resultCode, _ ->
					when(resultCode) {
						Activity.RESULT_OK, Activity.RESULT_FIRST_USER -> {
							try {
								val got = packageManager.getPackageInfo(info.packageName, PM_FLAGS)
								
								if(info.versionCode != got.versionCode) {
									future.fail(IllegalStateException("Failed to install an APK!"))
									return@startActivityForResult
								}
								
								initExtension(got, context)
								future.complete(getExtension(info.packageName))
							} catch(e: Throwable) {
								future.fail(e)
							}
						}
						
						Activity.RESULT_CANCELED -> future.fail(CancellationException("Install cancelled"))
					}
				})
			}
		} catch(e: IOException) {
			future.fail(e)
		}
	}

	override fun installExtension(context: Context, uri: Uri) = with(context.activity) {
		return@with AsyncUtils.controllableFuture { future ->
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()) {
				installApk(context, uri, future)
				return@controllableFuture
			}

			runOnUiThread {
				startActivityForResult(buildIntent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, getPackageUri()), { resultCode, _ ->
					when(resultCode) {
						Activity.RESULT_OK -> AsyncUtils.thread { installApk(context, uri, future) }
						Activity.RESULT_CANCELED -> future.fail(CancellationException("Permission denied!"))
						else -> future.fail(CancellationException("Failed to install an extension"))
					}
				})
			}
		}
	}

	override fun uninstallExtension(context: Context, id: String) = with(context.activity) {
		return@with AsyncUtils.controllableFuture { future ->
			runOnUiThread {
				startActivityForResult(buildIntent(Intent.ACTION_DELETE, getPackageUri()), { _, _ ->
					//Ignore the resultCode, it always equal to 0

					try {
						context.packageManager.getPackageInfo(id, 0)
						future.complete(false)
					} catch(e: PackageManager.NameNotFoundException) {
						//App info is no longer available, so it is uninstalled.
						extensions.remove(id)

						try {
							future.complete(true)
						} catch(ex: Throwable) {
							future.fail(ex)
						}
					} catch(e: Throwable) {
						future.fail(e)
					}
				})
			}
		}
	}
}